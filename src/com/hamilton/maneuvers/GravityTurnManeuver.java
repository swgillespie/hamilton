package com.hamilton.maneuvers;

import com.hamilton.common.Util;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Vessel;

import java.util.logging.Logger;

public class GravityTurnManeuver extends Maneuver {
  private static final Logger LOGGER = Logger.getLogger(GravityTurnManeuver.class.getName());

  @Override
  public String getName() {
    return "gravity turn";
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    LOGGER.info("Beginning gravity turn");
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    Vessel vessel = spaceCenter.getActiveVessel();
    CelestialBody celestialBody = vessel.getOrbit().getBody();
    GravityTurn gravityTurn = getGravityTurnForBody(vessel, celestialBody);

    Stream<Double> altitudeStream = conn.addStream(vessel.flight(vessel.getReferenceFrame()), "getSurfaceAltitude");
    double turnAngle = 0;
    while (Util.loopForever()) {
      double altitude = altitudeStream.get();
      if (gravityTurn.isComplete(altitude)) {
        break;
      }

      double newAngle = gravityTurn.getPitchAngle(altitude);
      if (Math.abs(newAngle - turnAngle) > 0.5) {
        vessel.getAutoPilot().targetPitchAndHeading((float)newAngle, 90);
        turnAngle = newAngle;
      }
    }
  }

  private GravityTurn getGravityTurnForBody(Vessel vessel, CelestialBody body) throws RPCException {
    // This sucks, but it works for now.
    switch (body.getName()) {
      case "Kerbin":
        LOGGER.info("Detected Kerbin launch");
        return new UniformGravityTurn(250.0, 45000.0);
      case "Mun":
        LOGGER.info("Detected Mun launch");
        return new UniformGravityTurn(0, 500);
      default:
        abort("no GravityTurn for body " + body.getName());
        return null;
    }
  }
}

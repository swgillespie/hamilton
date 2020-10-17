package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;

import java.util.logging.Logger;

public final class GravityTurn extends Stage {
  private static final Logger LOGGER = Logger.getLogger(GravityTurn.class.getName());

  private final double turnStartAltitude;
  private final double turnEndAltitude;

  public GravityTurn(double turnStartAltitude, double turnEndAltitude) {
    super("gravity turn");
    this.turnStartAltitude = turnStartAltitude;
    this.turnEndAltitude = turnEndAltitude;
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    Vessel vessel = SpaceCenter.newInstance(conn).getActiveVessel();
    Flight flight = vessel.flight(vessel.getReferenceFrame());

    LOGGER.info("Beginning uniform gravity turn from " + turnStartAltitude + " to " + turnEndAltitude);
    Stream<Double> altitude = conn.addStream(flight, "getMeanAltitude");
    double turnAngle = 0;
    while (Util.loopForever()) {
      if (altitude.get() > turnStartAltitude && altitude.get() < turnEndAltitude) {
        double frac = (altitude.get() - turnStartAltitude)
                / (turnEndAltitude - turnStartAltitude);
        double newTurnAngle = frac * 90.0;
        if (Math.abs(newTurnAngle - turnAngle) > 0.5) {
          turnAngle = newTurnAngle;
          vessel.getAutoPilot().targetPitchAndHeading(
                  (float) (90 - turnAngle), 90);
        }
      }

      if (altitude.get() > turnEndAltitude) {
        LOGGER.info("altitude > " + turnEndAltitude + ", gravity turn complete");
        return;
      }
    }
  }
}

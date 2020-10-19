package com.hamilton.maneuvers.hohmann;

import com.hamilton.common.OrbitMath;
import com.hamilton.common.Util;
import com.hamilton.maneuvers.Maneuver;
import krpc.client.Connection;
import krpc.client.Event;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.KRPC.Expression;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.schema.KRPC.ProcedureCall;
import org.javatuples.Triplet;

import java.util.logging.Logger;

public class PeriapsisBurnManeuver extends Maneuver {
  private static final Logger LOGGER = Logger.getLogger(PeriapsisBurnManeuver.class.getName());

  private final double targetApoapsisAltitude;

  public PeriapsisBurnManeuver(double targetApoapsisAltitude) {
    this.targetApoapsisAltitude = targetApoapsisAltitude;
  }

  @Override
  public String getName() {
    return "hohmann transfer, periapsis burn";
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    Vessel vessel = spaceCenter.getActiveVessel();
    Orbit orbit = vessel.getOrbit();
    KRPC krpc = KRPC.newInstance(conn);

    double deltaV = OrbitMath.hohmannTransferPeriapsisBurnCost(orbit, targetApoapsisAltitude);
    double burnTime = OrbitMath.burnTime(vessel, deltaV);
    double timeToPeriapsis = spaceCenter.getActiveVessel().getOrbit().getTimeToPeriapsis();
    Node node = vessel.getControl().addNode(spaceCenter.getUT() + timeToPeriapsis, (float) deltaV, 0, 0);
    LOGGER.info("Rotating to prograde for burn");
    vessel.getAutoPilot().setReferenceFrame(node.getReferenceFrame());
    vessel.getAutoPilot().setTargetDirection(new Triplet<>(0.0, 1.0, 0.0));
    vessel.getAutoPilot().wait_();

    LOGGER.info("Warping to periapsis");
    spaceCenter.warpTo(spaceCenter.getUT() + timeToPeriapsis - burnTime / 2, 100000, 4);

    LOGGER.info("Burning to raise apoapsis to " + targetApoapsisAltitude);
    vessel.getControl().setThrottle(1);
    Util.sleepSeconds(burnTime);
    vessel.getControl().setThrottle(0);

    LOGGER.info("Periapsis burn complete");
    node.remove();
  }
}

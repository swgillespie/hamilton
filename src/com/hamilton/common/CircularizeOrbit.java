package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Node;
import org.javatuples.Triplet;

import java.util.logging.Logger;

public final class CircularizeOrbit extends Stage {
  private static final Logger LOGGER = Logger.getLogger(CircularizeOrbit.class.getName());

  public CircularizeOrbit() {
    super("circularize orbit");
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    Vessel vessel = spaceCenter.getActiveVessel();

    double circularizationBurnDeltaV = OrbitMath.orbitCircularizationCost(vessel.getOrbit());
    LOGGER.info(circularizationBurnDeltaV + " m/s to circularize");
    double burnTime = OrbitMath.burnTime(vessel, circularizationBurnDeltaV);
    LOGGER.info(burnTime + "s burn time");
    Node node = vessel.getControl().addNode(spaceCenter.getUT() + vessel.getOrbit().getTimeToApoapsis(), (float) circularizationBurnDeltaV, 0, 0);

    LOGGER.info("Orienting vessel to circularization burn prograde");
    // Turn to face what will be prograde at the time of the maneuver node that we set.
    vessel.getAutoPilot().setReferenceFrame(node.getReferenceFrame());
    vessel.getAutoPilot().setTargetDirection(new Triplet<>(0.0, 1.0, 0.0));
    vessel.getAutoPilot().wait_();

    LOGGER.info("Warping to 5 seconds prior to circularization burn");
    // Warp until 5 seconds before the burn begins
    double burnUT = spaceCenter.getUT() + vessel.getOrbit().getTimeToApoapsis() - (burnTime / 2.0);
    double leadTime = 5;
    spaceCenter.warpTo(burnUT - leadTime, 100000.0f, 4.0f);

    Stream<Double> timeToApoapsis = conn.addStream(vessel.getOrbit(), "getTimeToApoapsis");
    LOGGER.info("Busy-waiting for start of burn");
    while (Util.loopForever()) {
      if (timeToApoapsis.get() - (burnTime / 2.0) < 0) {
        break;
      }
    }

    LOGGER.info("Beginning circularization burn (" + circularizationBurnDeltaV + " m/s, " + burnTime + "s burn)");
    vessel.getControl().setThrottle(1);
    Util.sleepSeconds(burnTime);
    vessel.getControl().setThrottle(0);
    LOGGER.info("Burn complete");
    vessel.getControl().removeNodes();
  }
}

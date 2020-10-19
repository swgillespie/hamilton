package com.hamilton.maneuvers;

import com.hamilton.common.OrbitMath;
import com.hamilton.common.Util;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;
import org.javatuples.Triplet;

import java.util.logging.Logger;

public class OrbitCircularizationManeuver extends Maneuver {
  private static final Logger LOGGER = Logger.getLogger(OrbitCircularizationManeuver.class.getName());

  @Override
  public String getName() {
    return "circularize orbit";
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

    // How far did we miss our goal?
    Stream<Double> remainingBurn = conn.addStream(node, "getRemainingDeltaV");
    double goalMiss = remainingBurn.get();
    LOGGER.info("Missed goal by " + goalMiss + "m/s");

    // Our fine-tuning burn is at 5% of full throttle, so multiply by 20 to get the burn time at 5% throttle.
    double burnTimeToFixGoal = OrbitMath.burnTime(vessel, goalMiss) * 20;
    if (burnTimeToFixGoal > 0.5) {
      LOGGER.info("Fine-tuning burn");
      vessel.getControl().setThrottle(0.05f);
      while (Util.loopForever() && remainingBurn.get() > 5.0) {
        LOGGER.fine("remaining burn: " + remainingBurn.get() + "m/s");
      }
    } else {
      LOGGER.info("Not fine-tuning burn, we got close enough");
    }

    vessel.getControl().setThrottle(0);
    LOGGER.info("Burn complete");
    node.remove();
  }
}

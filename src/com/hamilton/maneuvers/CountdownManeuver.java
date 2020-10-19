package com.hamilton.maneuvers;

import com.hamilton.common.Util;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.AutoPilot;

import java.util.logging.Logger;

public class CountdownManeuver extends Maneuver {
  private static final Logger LOGGER = Logger.getLogger(CountdownManeuver.class.getName());

  @Override
  public String getName() {
    return "countdown";
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    Vessel vessel = SpaceCenter.newInstance(conn).getActiveVessel();
    LOGGER.info("Initiating countdown, pitching up");
    AutoPilot ap = vessel.getAutoPilot();
    ap.targetPitchAndHeading(90, 90);
    ap.engage();

    for (int i = 5; i > 0; i--) {
      LOGGER.info(i + "...");
      Util.sleepSeconds(1);
    }

    vessel.getControl().setThrottle(1);
    LOGGER.info("Liftoff!");
  }
}

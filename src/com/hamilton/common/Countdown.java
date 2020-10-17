package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.AutoPilot;

import java.util.logging.Logger;

public final class Countdown extends Stage {
  private static final Logger LOGGER = Logger.getLogger(Countdown.class.getName());

  public Countdown() {
    super("countdown");
  }

  @Override
  public void execute(Connection krpcClient) throws RPCException {
    Vessel vessel = SpaceCenter.newInstance(krpcClient).getActiveVessel();
    AutoPilot ap = vessel.getAutoPilot();
    LOGGER.info("Pitching up to (90, 90), setting throttle to 100%");
    ap.targetPitchAndHeading(90, 90);
    ap.engage();
    vessel.getControl().setThrottle(1);
    for (int i = 5; i > 0; i--) {
      LOGGER.info(i + "...");
      Util.sleepSeconds(1);
    }

    vessel.getControl().activateNextStage();
    LOGGER.info("Liftoff!");
  }
}

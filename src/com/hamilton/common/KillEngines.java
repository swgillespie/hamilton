package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;

import java.util.logging.Logger;

public final class KillEngines extends Stage {
  private static final Logger LOGGER = Logger.getLogger(KillEngines.class.getName());

  public KillEngines() {
    super("kill engines");
  }

  @Override
  public void execute(Connection krpcClient) throws RPCException, StreamException {
    LOGGER.info("Setting throttle to 0");
    Vessel vessel = SpaceCenter.newInstance(krpcClient).getActiveVessel();
    vessel.getControl().setThrottle(0);
  }
}

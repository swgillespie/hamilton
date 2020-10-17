package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;

import java.util.logging.Logger;

public final class Recover extends Stage {
  private static final Logger LOGGER = Logger.getLogger(Recover.class.getName());

  public Recover() {
    super("recover vessel");
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    Vessel vessel = SpaceCenter.newInstance(conn).getActiveVessel();
    LOGGER.info("Sleeping 5 seconds to give vessel time to settle");
    Util.sleepSeconds(5);
    vessel.recover();
  }
}

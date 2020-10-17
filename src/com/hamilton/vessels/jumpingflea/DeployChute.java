package com.hamilton.vessels.jumpingflea;

import com.hamilton.common.Stage;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter;

import java.util.logging.Logger;

public final class DeployChute extends Stage {
  private static final Logger LOGGER = Logger.getLogger(DeployChute.class.getName());

  public DeployChute() {
    super("deploy chute");
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    Vessel vessel = SpaceCenter.newInstance(conn).getActiveVessel();
    vessel.getControl().activateNextStage();
  }
}

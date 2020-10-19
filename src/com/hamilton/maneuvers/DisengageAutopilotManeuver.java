package com.hamilton.maneuvers;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;

public class DisengageAutopilotManeuver extends Maneuver {
  @Override
  public String getName() {
    return "disengage autopilot";
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    SpaceCenter.newInstance(conn).getActiveVessel().getAutoPilot().disengage();
  }
}

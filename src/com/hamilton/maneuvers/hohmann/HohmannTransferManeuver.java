package com.hamilton.maneuvers.hohmann;

import com.hamilton.maneuvers.CompositeManeuver;
import com.hamilton.maneuvers.EngageAutopilotManeuver;
import com.hamilton.maneuvers.Maneuver;
import com.hamilton.maneuvers.OrbitCircularizationManeuver;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public class HohmannTransferManeuver extends Maneuver {
  private final double targetOrbitAltitude;

  public HohmannTransferManeuver(double targetOrbitAltitude) {
    this.targetOrbitAltitude = targetOrbitAltitude;
  }

  @Override
  public String getName() {
    return "hohmann transfer";
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    CompositeManeuver maneuver = new CompositeManeuver();
    maneuver.addManeuver(new EngageAutopilotManeuver());
    maneuver.addManeuver(new PeriapsisBurnManeuver(targetOrbitAltitude));
    maneuver.addManeuver(new OrbitCircularizationManeuver());
    maneuver.execute(conn);
  }
}

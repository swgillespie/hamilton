package com.hamilton.vessels.kerbalx;

import com.hamilton.common.*;
import com.hamilton.vessels.HamiltonVessel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.io.IOException;

/**
 * The KerbalX is a stock rocket that features a sort-of unusual asparagus stack of liquid rockets as a first stage.
 * To accomodate this, the OuterRockets custom stage performs the asparagus stage in parallel with the gravity turn.
 * Once the gravity turn is done, we can get up to orbit pretty much like any other rocket.
 */
public final class KerbalX extends HamiltonVessel {
  public static void main(String[] args) throws IOException, RPCException, StreamException {
    KerbalX vessel = new KerbalX();
    try (Connection connection = Connection.newInstance(vessel.getName())) {
      vessel.run(connection);
    }
  }

  @Override
  protected Stage[] getStages(Connection conn) throws RPCException {
    return new Stage[]{
      new Countdown(),
      new ParallelStage(
        new GravityTurn(250, 45000),
        new OuterRockets()
      ),
      new KillEngines(),
      Wait.forAtmosphericDeparture(conn),
      new RaiseApoapsis(150000),
      new CircularizeOrbit(),
    };
  }

  @Override
  protected String getName() {
    return "Kerbal X";
  }
}

package com.hamilton.vessels.jumpingflea;

import com.hamilton.common.Countdown;
import com.hamilton.common.Recover;
import com.hamilton.common.Stage;
import com.hamilton.common.Wait;
import com.hamilton.vessels.HamiltonVessel;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Onboard computer for the "Jumping Flea" vessel that ships with stock KSP.
 * <p>
 * The Jumping Flea doesn't get to orbit and doesn't even try. It launches straight up, falls for a bit, deploys its
 * parachute, lands, and is recovered. This computer results in a fully-automated flight.
 */
public final class JumpingFlea extends HamiltonVessel {
  private static final Logger LOGGER = Logger.getLogger(JumpingFlea.class.getName());

  public static void main(String[] args) throws IOException, RPCException, StreamException {
    JumpingFlea vessel = new JumpingFlea();
    try (Connection connection = Connection.newInstance(vessel.getName())) {
      vessel.run(connection);
    }
  }

  @Override
  protected Stage[] getStages(Connection conn) throws RPCException {
    return new Stage[]{
      new Countdown(),
      Wait.forSurfaceAltitudeGreaterThan(conn, 5000),
      Wait.forSurfaceAltitudeLessThan(conn, 2000),
      new DeployChute(),
      Wait.forLanding(conn),
      new Recover()
    };
  }

  @Override
  protected String getName() {
    return "Jumping Flea";
  }
}

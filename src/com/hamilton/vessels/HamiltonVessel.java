package com.hamilton.vessels;

import com.hamilton.common.Stage;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.util.logging.Logger;

/**
 * A HamiltonVessel is the onboard computer for a particular vessel. Since vessel in KSP are custom, each vessel
 * generally needs some custom code to function. All vessels derive from HamiltonVessel.
 * <p>
 * The core of a HamiltonVessel is `getStages`, where a vessel can explicitly declare its list of stages.
 */
public abstract class HamiltonVessel {
  private final Logger LOGGER = Logger.getLogger(HamiltonVessel.class.getName());

  protected abstract Stage[] getStages(Connection conn) throws RPCException;

  protected abstract String getName();

  public final void run(Connection krpcClient) throws RPCException, StreamException {
    LOGGER.info("Running vessel \"" + getName() + "\"");
    Stage[] stages = getStages(krpcClient);
    for (Stage stage : stages) {
      LOGGER.info("Running stage: " + stage.getName());
      stage.execute(krpcClient);
    }
  }
}

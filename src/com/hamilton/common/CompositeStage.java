package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.util.logging.Logger;

public abstract class CompositeStage extends Stage {
  private static final Logger LOGGER = Logger.getLogger(CompositeStage.class.getName());

  public CompositeStage(String name) {
    super(name);
  }

  @Override
  public final void execute(Connection conn) throws RPCException, StreamException {
    LOGGER.info("executing composite stage " + getName());
    for (Stage stage : getStages(conn)) {
      stage.execute(conn);
    }
  }

  protected abstract Stage[] getStages(Connection conn) throws RPCException;
}

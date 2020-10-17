package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

public abstract class Stage {
  private final String name;

  public Stage(String name) {
    this.name = name;
  }

  public final String getName() {
    return name;
  }

  public abstract void execute(Connection krpcClient) throws RPCException, StreamException;
}

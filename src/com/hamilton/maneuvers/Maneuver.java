package com.hamilton.maneuvers;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * A Maneuver is a packaged program that takes an existing KSP vessel, assumes control of it, performs some sort of
 * orbital maneuver on it, and then relinquishes control. Maneuvers can be executed standalone, or they can be chained
 * together. The first maneuvers are standalone.
 *
 * Maneuvers are designed to be vessel-independent! Any vessel-specific code needs to be in the `com.hamilton.vessels`
 * directory and referenced here by abstraction.
 */
public abstract class Maneuver {
  public abstract String getName();

  public abstract void execute(Connection conn) throws RPCException, StreamException;

  /**
   * Executes a maneuver asynchronously, returning a Future that is completed when the maneuver finishes execution.
   */
  public CompletableFuture<Void> executeAsync(Connection conn, Executor executor) {
    return CompletableFuture.runAsync(() -> {
      try {
        this.execute(conn);
      } catch (RPCException | StreamException e) {
        throw new RuntimeException(e);
      }
    }, executor);
  }

  protected final void abort(String reason) {
    throw new AbortedManeuverException(reason);
  }
}

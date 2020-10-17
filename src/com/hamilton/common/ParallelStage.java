package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public final class ParallelStage extends Stage {
  private static final Logger LOGGER = Logger.getLogger(ParallelStage.class.getName());
  private static final ExecutorService EXECUTOR = Executors.newWorkStealingPool(32);

  private final Stage left;
  private final Stage right;

  public ParallelStage(Stage left, Stage right) {
    super("parallel");
    this.left = left;
    this.right = right;
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    Future<Boolean> leftThread = EXECUTOR.submit(() -> {
      try {
        left.execute(conn);
      } catch (RPCException | StreamException e) {
        LOGGER.warning(e.toString());
        throw new RuntimeException(e);
      }

      return true;
    });

    Future<Boolean> rightThread = EXECUTOR.submit(() -> {
      try {
        right.execute(conn);
      } catch (RPCException | StreamException e) {
        LOGGER.warning(e.toString());
        throw new RuntimeException(e);
      }

      return true;
    });

    try {
      leftThread.get();
      rightThread.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.warning(e.toString());
      throw new RuntimeException(e);
    }
  }
}

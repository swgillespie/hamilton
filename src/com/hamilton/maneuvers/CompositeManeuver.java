package com.hamilton.maneuvers;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * A CompositeManeuver is a higher-level maneuver that is made up of smaller maneuvers that are executed in seuqence.
 */
public class CompositeManeuver extends Maneuver {
  private final static Logger LOGGER = Logger.getLogger(CompositeManeuver.class.getName());
  private final static Executor EXECUTOR = Executors.newFixedThreadPool(8);

  private final ArrayList<ArrayList<Maneuver>> maneuvers = new ArrayList<>();
  private ArrayList<Maneuver> parallelManeuvers = null;

  @Override
  public String getName() {
    return "composite";
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(Connection conn) throws RPCException, StreamException {
    for (ArrayList<Maneuver> maneuver : maneuvers) {
      CompletableFuture<Void>[] parallelManeuvers = maneuver.stream().map(m -> m.executeAsync(conn, EXECUTOR)).toArray(CompletableFuture[]::new);
      try {
        CompletableFuture.allOf(parallelManeuvers).get();
      } catch (InterruptedException e) {
        LOGGER.info("interrupted: " + e);
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Adds a maneuver to the composite maneuver. Not valid once executing!
   *
   * @param maneuver The maneuver to add to the sequence.
   */
  public void addManeuver(Maneuver maneuver) {
    assert parallelManeuvers == null;
    beginParallelBlock();
    parallelManeuvers.add(maneuver);
    endParallelBlock();
  }

  public void addParallelManeuver(Maneuver maneuver) {
    assert parallelManeuvers != null;
    parallelManeuvers.add(maneuver);
  }

  public void beginParallelBlock() {
    parallelManeuvers = new ArrayList<>();
  }

  public void endParallelBlock() {
    maneuvers.add(parallelManeuvers);
    parallelManeuvers = null;
  }
}

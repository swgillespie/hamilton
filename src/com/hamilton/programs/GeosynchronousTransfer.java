package com.hamilton.programs;

import com.hamilton.maneuvers.hohmann.HohmannTransferManeuver;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.io.IOException;

/**
 * Transfers the current orbit to a geosynchronous orbit above the currently-orbited body.
 */
public class GeosynchronousTransfer {
  public static void main(String[] args) throws IOException, RPCException, StreamException {
    HohmannTransferManeuver maneuver = new HohmannTransferManeuver(3463330);
    try (Connection connection = Connection.newInstance(maneuver.getName())) {
      maneuver.execute(connection);
    }
  }
}

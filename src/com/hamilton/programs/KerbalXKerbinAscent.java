package com.hamilton.programs;

import com.hamilton.maneuvers.Ascent;
import com.hamilton.vessels.kerbalx.KerbalXAsparagusStagingManeuver;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

import java.io.IOException;

/**
 * Program to perform an ascent from Kerbin using the Kerbal X rocket to an orbit of 150km.
 */
public class KerbalXKerbinAscent {
  public static void main(String[] args) throws IOException, RPCException, StreamException {
    Ascent ascent = new Ascent(150000, new KerbalXAsparagusStagingManeuver());
    try (Connection connection = Connection.newInstance(ascent.getName())) {
      ascent.execute(connection);
    }
  }
}

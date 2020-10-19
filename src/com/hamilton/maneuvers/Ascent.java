package com.hamilton.maneuvers;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.StreamException;

/**
 * The Ascent maneuver launches from sea level of whatever body the vessel is currently on into a circular orbit of
 * customizable altitude.
 */
public class Ascent extends Maneuver {
  private final double targetOrbitAltitude;
  private final Maneuver stagingManeuver;

  public Ascent(double targetOrbitAltitude, Maneuver stagingManeuver) {
    this.targetOrbitAltitude = targetOrbitAltitude;
    this.stagingManeuver = stagingManeuver;
  }

  @Override
  public String getName() {
    return "ascent";
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    // Ascent has four stages:
    //
    // 1. Engine ignition and immediate launch. There's a short countdown here so that the human running this program
    //    can abort if they want to.
    // 2. A gravity turn. Depending on the atmosphere of the body we're taking off from, this might be a gradual turn
    //    that aims to be horizontal high in the body's atmosphere, or an immediate gravity turn that goes horizontal
    //    almost immediately upon launch.
    // 3. Raising the apoapsis to the target orbit altitude. If the body we're launching from has atmosphere, the vessel
    //    will coast until it leaves atmosphere prior to doing an apoapsis-raising burn.
    // 4. Circularizing the orbit at the target altitude. This plans and executes a burn at the apoapsis of the
    //    sub-orbital trajectory that the apoapsis-raising burn put us on.
    //
    // These are mostly vessel independent. The main thing that differs from vessel to vessel is staging.
    CompositeManeuver ascentManeuver = new CompositeManeuver();

    // Step 1: The countdown and immediate launch.
    ascentManeuver.addManeuver(new CountdownManeuver());

    // Step 2: The gravity turn. Done in parallel with staging.
    ascentManeuver.beginParallelBlock();
    ascentManeuver.addParallelManeuver(new GravityTurnManeuver());
    ascentManeuver.addParallelManeuver(stagingManeuver);
    ascentManeuver.endParallelBlock();

    // Step 3: Raising the apoapsis.
    ascentManeuver.addManeuver(new RaiseApoapsisManeuver(targetOrbitAltitude));

    // Step 4: Orbit circularization.
    ascentManeuver.addManeuver(new OrbitCircularizationManeuver());

    // Disengage the autopilot when we're done so the human can take over.
    ascentManeuver.addManeuver(new DisengageAutopilotManeuver());

    // Let's go!
    ascentManeuver.execute(conn);
  }
}

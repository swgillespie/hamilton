package com.hamilton.common;

import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.Vessel;

/**
 * OrbitMath is a home for the various calculations that are useful when doing orbital math.
 */
public final class OrbitMath {
  /**
   * Calculates the cost, in m/s (delta-V), to circularize a given orbit. The orbit circularization burn is assumed to
   * happen at the apoapsis (as it should).
   *
   * @param orbit The KSP orbit for a vessel
   * @return The delta-V required to circularize this orbit
   */
  public static double orbitCircularizationCost(Orbit orbit) throws RPCException {
    // This is the vis-viva equation: https://en.wikipedia.org/wiki/Vis-viva_equation
    //
    // The standard gravitational parameter, or mu. In the vis-viva equation, this is the GM term (the gravitational
    // constant multiplied by the mass of the body).
    //
    // I don't think bodies in KSP actually have mass, so the gravitational parameter is what we get.
    double mu = orbit.getBody().getGravitationalParameter();

    // The distance between the two bodies; in this case, the vessel and the body it orbits. Since we're circularizing
    // the orbit, the distance is constant and it's the current apoapsis.
    double r = orbit.getApoapsis();

    // This orbit is elliptical in some way. The semimajor axis is the width of the orbit at its longest point. We're
    // going to use the vis-viva equation to calculate the orbital velocity at the point of the semimajor axis.
    double a1 = orbit.getSemiMajorAxis();

    // At the other point is the apoapsis. Since we want the orbit to be circular, we'll calculate the orbital velocity
    // at this point as well.
    double a2 = r;

    // How much do we need to change the velocity such that the semimajor axis's velocity is equal to the apoapsis? That
    // number is the delta-v that we'll need to use at the apoapsis to circularize the orbit.
    double v1 = Math.sqrt(mu * ((2.0 / r) - (1.0 / a1)));
    double v2 = Math.sqrt(mu * ((2.0 / r) - (1.0 / a2)));
    return v2 - v1;
  }

  /**
   * Calculates the burn time for the given vessel in order to accomplish a burn with the given delta-V.
   *
   * @param vessel The vessel that will be performing the burn
   * @param deltaV The desired delta-V of the burn
   * @return The duration, in seconds, of the burn
   */
  public static double burnTime(Vessel vessel, double deltaV) throws RPCException {
    // This is the classical rocket equation: https://en.wikipedia.org/wiki/Tsiolkovsky_rocket_equation
    //
    // The magnitude of the thrust vector available to the vessel.
    double force = vessel.getAvailableThrust();

    // The specific impulse of the engines available to the vessel, multiplied by the acceleration due to gravity of
    // the body that the vessel currently orbits. Together, this forms the "effective exhaust velocity".
    double isp = vessel.getSpecificImpulse() * vessel.getOrbit().getBody().getSurfaceGravity();

    // The current mass of the vessel, including fuel that will be expended during the burn.
    double m0 = vessel.getMass();

    // The mass of the vessel, after the burn is complete. Fuel is not included because it has all been burned.
    double m1 = m0 / Math.exp(deltaV / isp);
    double flowRate = force / isp;
    return (m0 - m1) / flowRate;
  }
}

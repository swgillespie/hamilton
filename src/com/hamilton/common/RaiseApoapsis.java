package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.Event;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.schema.KRPC.ProcedureCall;
import krpc.client.services.KRPC.Expression;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.Node;

import java.util.logging.Logger;

/**
 * Assuming that the current vessel is on a sub-orbital trajectory outside of a body's atmosphere, RaiseApoapsis raises
 * the current apoapsis to the given target.
 */
public final class RaiseApoapsis extends Stage {
  private final static Logger LOGGER = Logger.getLogger(RaiseApoapsis.class.getName());

  private final double targetApoapsis;

  public RaiseApoapsis(double targetApoapsis) {
    super("raise apoapsis");
    this.targetApoapsis = targetApoapsis;
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    LOGGER.info("Raising apoapsis to " + targetApoapsis);
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    Vessel vessel = spaceCenter.getActiveVessel();

    // Don't max out the throttle; we're in space and this is a delicate burn.
    vessel.getControl().setThrottle(0.5f);
    ProcedureCall apoapsisAltitude = conn.getCall(vessel.getOrbit(), "getApoapsisAltitude");
    Expression expr = Expression.greaterThan(conn, Expression.call(conn, apoapsisAltitude), Expression.constantDouble(conn, targetApoapsis));
    KRPC krpc = KRPC.newInstance(conn);
    Event event = krpc.addEvent(expr);
    synchronized (event.getCondition()) {
      event.waitFor();
    }

    // Kill the throttle and coast.
    vessel.getControl().setThrottle(0.0f);

    LOGGER.info("Killed engines, apoapsis is " + vessel.getOrbit().getApoapsisAltitude());
    double circularizationBurnDeltaV = OrbitMath.orbitCircularizationCost(vessel.getOrbit());
    LOGGER.info(circularizationBurnDeltaV + " m/s to circularize");
    double burnTime = OrbitMath.burnTime(vessel, circularizationBurnDeltaV);
    LOGGER.info(burnTime + "s burn time");
    vessel.getControl().addNode(spaceCenter.getUT() + vessel.getOrbit().getTimeToApoapsis(), (float) circularizationBurnDeltaV, 0, 0);
  }
}

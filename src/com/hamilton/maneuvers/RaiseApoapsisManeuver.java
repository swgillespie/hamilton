package com.hamilton.maneuvers;

import krpc.client.Connection;
import krpc.client.Event;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.KRPC.Expression;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.schema.KRPC.ProcedureCall;

import java.util.logging.Logger;

public class RaiseApoapsisManeuver extends Maneuver {
  private static final Logger LOGGER = Logger.getLogger(RaiseApoapsisManeuver.class.getName());

  private final double targetApoapsisAltitude;

  public RaiseApoapsisManeuver(double targetApoapsisAltitude) {
    this.targetApoapsisAltitude = targetApoapsisAltitude;
  }

  @Override
  public String getName() {
    return "raise apoapsis";
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    KRPC krpc = KRPC.newInstance(conn);
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    Vessel vessel = spaceCenter.getActiveVessel();
    ReferenceFrame vesselFrame = vessel.getReferenceFrame();

    LOGGER.info("Raising apoapsis to " + targetApoapsisAltitude);
    // If we're in atmosphere, kill the engine and coast until we aren't anymore. Burning in space is always cheaper
    // than burning in atmosphere due to drag.
    if (vessel.flight(vesselFrame).getAtmosphereDensity() > 0) {
      LOGGER.info("Vessel is in atmosphere, killing engines and coasting until atmospheric departure");
      vessel.getControl().setThrottle(0);
      ProcedureCall atmosphereDensity = conn.getCall(vessel.flight(vesselFrame), "getAtmosphereDensity");
      Expression expr = Expression.equal(conn, Expression.call(conn, atmosphereDensity), Expression.constantFloat(conn, 0));
      Event event = krpc.addEvent(expr);
      synchronized (event.getCondition()) {
        event.waitFor();
      }
    }

    // Burn until our apoapsis is what we want it to be. Don't max out the throttle; we're in space and this is a delicate burn.
    LOGGER.info("Burning until our apoapsis is " + targetApoapsisAltitude);
    vessel.getControl().setThrottle(0.5f);
    ProcedureCall apoapsisAltitude = conn.getCall(vessel.getOrbit(), "getApoapsisAltitude");
    Expression altitudeExpr = Expression.greaterThan(conn, Expression.call(conn, apoapsisAltitude), Expression.constantDouble(conn, targetApoapsisAltitude));
    Event altitudeEvent = krpc.addEvent(altitudeExpr);
    synchronized (altitudeEvent.getCondition()) {
      altitudeEvent.waitFor();
    }

    vessel.getControl().setThrottle(0);
    LOGGER.info("Burn complete");
  }
}

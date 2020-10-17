package com.hamilton.common;

import krpc.client.Connection;
import krpc.client.Event;
import krpc.client.RPCException;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.KRPC.Expression;
import krpc.client.services.SpaceCenter;
import krpc.schema.KRPC.ProcedureCall;

import java.util.logging.Logger;

public final class Wait {
  private static final Logger LOGGER = Logger.getLogger(Wait.class.getName());

  public static Stage forExpression(Connection conn, Expression expr, String stageName) throws RPCException {
    KRPC krpc = KRPC.newInstance(conn);
    Event event = krpc.addEvent(expr);
    return new Stage(stageName) {
      @Override
      public void execute(Connection krpcClient) throws RPCException, StreamException {
        LOGGER.info("Waiting for " + stageName);
        synchronized (event.getCondition()) {
          event.waitFor();
        }
      }
    };
  }

  public static Stage forSurfaceAltitudeGreaterThan(Connection conn, double altitude) throws RPCException {
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    ProcedureCall surfaceAltitude = conn.getCall(spaceCenter.getActiveVessel().flight(null), "getSurfaceAltitude");
    Expression expr = Expression.greaterThan(conn, Expression.call(conn, surfaceAltitude), Expression.constantDouble(conn, altitude));
    return forExpression(conn, expr, "surface_altitude > " + altitude);
  }

  public static Stage forSurfaceAltitudeLessThan(Connection conn, double altitude) throws RPCException {
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    ProcedureCall surfaceAltitude = conn.getCall(spaceCenter.getActiveVessel().flight(null), "getSurfaceAltitude");
    Expression expr = Expression.lessThan(conn, Expression.call(conn, surfaceAltitude), Expression.constantDouble(conn, altitude));
    return forExpression(conn, expr, "surface_altitude < " + altitude);
  }

  public static Stage forLanding(Connection conn) throws RPCException {
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    ProcedureCall verticalSpeed = conn.getCall(spaceCenter.getActiveVessel().flight(null), "getVerticalSpeed");
    ProcedureCall surfaceAltitude = conn.getCall(spaceCenter.getActiveVessel().flight(null), "getSurfaceAltitude");
    Expression expr = Expression.and(conn,
      Expression.lessThan(conn, Expression.call(conn, verticalSpeed), Expression.constantDouble(conn, 0.5)),
      Expression.lessThan(conn, Expression.call(conn, surfaceAltitude), Expression.constantDouble(conn, 10)));
    return forExpression(conn, expr, "landing on current body");
  }

  public static Stage forAtmosphericDeparture(Connection conn) throws RPCException {
    SpaceCenter spaceCenter = SpaceCenter.newInstance(conn);
    ProcedureCall atmosphereDensity = conn.getCall(spaceCenter.getActiveVessel().flight(null), "getAtmosphereDensity");
    Expression expr = Expression.equal(conn, Expression.call(conn, atmosphereDensity), Expression.constantFloat(conn, 0));
    return forExpression(conn, expr, "departing current body's atmosphere");
  }
}

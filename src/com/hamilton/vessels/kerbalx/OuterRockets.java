package com.hamilton.vessels.kerbalx;

import com.hamilton.common.Stage;
import com.hamilton.common.Util;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;

import java.util.logging.Logger;

/**
 * The KerbalX rocket has six liquid rockets surrounding a single, much larger rocket. The six rockets are designed to
 * be staged in an asparagus staging setup (https://wiki.kerbalspaceprogram.com/wiki/Asparagus_staging), so we don't
 * have traditional stages to query here - according to the game, stages 7, 6, 5 don't actually contain any fuel.
 * <p>
 * To deal with this complexity, this stage monitors the total amount of fuel on the ship and stages based on that.
 */
public final class OuterRockets extends Stage {
  private static final Logger LOGGER = Logger.getLogger(OuterRockets.class.getName());
  private static final float TOTAL_LIQUID_FUEL = 8280;
  private static final float OUTER_TANK_SIZE = 180 * 3; // Three tanks, each 180 fuel in size

  public OuterRockets() {
    super("outer rockets");
  }

  @Override
  public void execute(Connection conn) throws RPCException, StreamException {
    LOGGER.info("Watching total fuel level to ditch outer rockets");
    Vessel vessel = SpaceCenter.newInstance(conn).getActiveVessel();
    Resources resources = vessel.getResources();
    Stream<Float> liquidFuel = conn.addStream(resources, "amount", "LiquidFuel");
    int timesStaged = 0;
    while (Util.loopForever()) {
      float fuel = liquidFuel.get();
      if (fuel < TOTAL_LIQUID_FUEL - (timesStaged + 1) * 2 * OUTER_TANK_SIZE) {
        LOGGER.info("Dropping exhausted outer rocket");
        vessel.getControl().activateNextStage();
        timesStaged++;
      }

      if (timesStaged == 3) {
        LOGGER.info("All outer rockets dropped");
        return;
      }
    }
  }
}

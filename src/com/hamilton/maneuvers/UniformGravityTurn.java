package com.hamilton.maneuvers;

public class UniformGravityTurn implements GravityTurn {
  private final double turnStartAltitude;
  private final double turnEndAltitude;

  public UniformGravityTurn(double turnStartAltitude, double turnEndAltitude) {
    this.turnStartAltitude = turnStartAltitude;
    this.turnEndAltitude = turnEndAltitude;
  }

  @Override
  public double getPitchAngle(double altitude) {
    assert !isComplete(altitude);
    if (altitude < turnStartAltitude) {
      return 90.0;
    }

    // This gravity turn is asimple uniform function varying the pitch from
    // 90 to 0 from 250m to 45km.
    return 90 - ((altitude - turnStartAltitude) / (turnEndAltitude - turnStartAltitude)) * 90;
  }

  @Override
  public boolean isComplete(double altitude) {
    return altitude > turnEndAltitude;
  }
}

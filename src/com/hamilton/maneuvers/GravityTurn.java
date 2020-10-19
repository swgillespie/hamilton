package com.hamilton.maneuvers;

public interface GravityTurn {
  double getPitchAngle(double altitude);

  boolean isComplete(double altitude);
}

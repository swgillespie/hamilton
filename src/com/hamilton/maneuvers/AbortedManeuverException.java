package com.hamilton.maneuvers;

public class AbortedManeuverException extends RuntimeException {
  public AbortedManeuverException(String message) {
    super(message);
  }
}

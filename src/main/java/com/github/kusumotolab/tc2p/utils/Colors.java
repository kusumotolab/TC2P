package com.github.kusumotolab.tc2p.utils;

public enum Colors {
  RED("\u001b[00;31m"),
  GREEN("\u001b[00;32m"),
  YELLOW("\u001b[00;33m"),
  PURPLE("\u001b[00;34m"),
  PINK("\u001b[00;35m"),
  CYAN("\u001b[00;36m");

  private final String colorCode;
  private static final String endCode = "\u001b[00m";

  Colors(final String colorCode) {
    this.colorCode = colorCode;
  }

  public static String red(final String text) {
    return toColorCode(RED, text);
  }

  public static String green(final String text) {
    return toColorCode(GREEN, text);
  }

  public static String yellow(final String text) {
    return toColorCode(YELLOW, text);
  }

  public static String purple(final String text) {
    return toColorCode(PURPLE, text);
  }

  public static String pink(final String text) {
    return toColorCode(PINK, text);
  }

  public static String cyan(final String text) {
    return toColorCode(CYAN, text);
  }

  private static String toColorCode(final Colors color, final String text) {
    return color.colorCode + text + endCode;
  }
}

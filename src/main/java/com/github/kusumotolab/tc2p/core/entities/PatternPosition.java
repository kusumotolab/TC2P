package com.github.kusumotolab.tc2p.core.entities;

import lombok.Data;

@Data
public class PatternPosition {

  private final String url;
  private final String mjavaDiff;

  public static PatternPosition parse(final String line) {
    final String url = line.split(" ")[1];
    final String mjavaDiff = line.split("//")[1];
    return new PatternPosition(url, mjavaDiff);
  }
}

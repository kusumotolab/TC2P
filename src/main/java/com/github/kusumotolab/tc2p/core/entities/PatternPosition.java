package com.github.kusumotolab.tc2p.core.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class PatternPosition {

  private final String url;
  private final String mjavaDiff;

  public static PatternPosition parse(final String line) {
    final String url = line.split(" ")[0];
    final String mjavaDiff = line.split("//")[2];
    return new PatternPosition(url, mjavaDiff);
  }
}

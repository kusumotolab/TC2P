package com.github.kusumotolab.tc2p.core.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id"})
public class BaseLabel {

  private final int id;
  private final ActionEnum action;
  private final String type;

}

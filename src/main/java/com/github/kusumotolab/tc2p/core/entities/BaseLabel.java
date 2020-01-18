package com.github.kusumotolab.tc2p.core.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(exclude = {"id"})
public class BaseLabel {
  @Getter private final int id;
  @Getter private final ActionEnum action;
  @Getter private final String type;
}

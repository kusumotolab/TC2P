package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import lombok.Data;

@Data
public class ItemAndOccurrence<Item> {

  private final Item item;
  private final Occurrence occurrence;
}

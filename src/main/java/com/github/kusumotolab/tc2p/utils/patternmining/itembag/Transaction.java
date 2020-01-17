package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import java.util.List;
import lombok.Data;

@Data
public class Transaction<T> {

  private final TransactionID id;
  private final List<ItemAndOccurrence<T>> itemAndOccurrences;

}

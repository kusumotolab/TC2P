package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import java.util.Set;
import com.google.common.collect.Sets;
import lombok.Data;

@Data
class Occurrences {
  private final Set<Occurrence> occurrences = Sets.newHashSet();
}

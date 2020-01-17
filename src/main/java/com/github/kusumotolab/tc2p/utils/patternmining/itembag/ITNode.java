package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;

@Data
public class ITNode<T> {

  private final Set<T> itemSet;
  private final Set<TransactionID> transactionIds;
  private final Map<TransactionID, Map<T, Occurrences>> transactionIDOccurrencesMap;

  private final List<ITNode<T>> children = Lists.newArrayList();

  public static <T> ITNode<T> createRoot() {
    return new ITNode<>(Sets.newHashSet(), Sets.newHashSet(), Maps.newHashMap());
  }

  public int size() {
    return children.stream()
        .map(ITNode::size)
        .reduce(1, Integer::sum);
  }

}

package com.github.kusumotolab.tc2p.utils.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ComparableObject<T> {

  private final T t1;
  private final T t2;
  private final List<ComparablePair> list = new ArrayList<>();

  public ComparableObject(final T t1, final T t2) {
    this.t1 = t1;
    this.t2 = t2;
  }

  static <T> ComparableObject<T> create(final T t1, final T t2) {
    return new ComparableObject<>(t1, t2);
  }

  public <U> ComparableObject<T> setComparativeValue(final Function<T, U> function) {
    final U u1 = function.apply(t1);
    final U u2 = function.apply(t2);
    list.add(ComparablePair.create(u1, u2));
    return this;
  }

  public <U> ComparableObject<T> setComparativeValue(final Function<T, U> function, final
  BiFunction<U, U, Boolean> comparator) {
    final U u1 = function.apply(t1);
    final U u2 = function.apply(t2);
    list.add(ComparablePair.create(u1, u2, comparator));
    return this;
  }

  public boolean judge() {
    return list.stream()
        .allMatch(ComparablePair::equal);
  }
}

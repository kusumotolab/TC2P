package com.github.kusumotolab.tc2p.utils.compare;

import java.util.function.BiFunction;

public class ComparablePair<T> {
  private final T t1;
  private final T t2;
  private final BiFunction<T, T, Boolean> comparator;

  public ComparablePair(final T t1, final T t2, final BiFunction<T, T, Boolean> comparator) {
    this.t1 = t1;
    this.t2 = t2;
    this.comparator = comparator;
  }

  static <T> ComparablePair<T> create(final T t1, final T t2, final BiFunction<T, T, Boolean> comparator) {
    return new ComparablePair<>(t1, t2, comparator);
  }

  static <T> ComparablePair<T> create(final T t1, final T t2) {
    return new ComparablePair<>(t1, t2, Object::equals);
  }

  boolean equal() {
    if (t1 == null && t2 == null) {
      return true;
    }
    if (t1 == null || t2 == null) {
      return false;
    }
    return comparator.apply(t1, t2);
  }
}

package com.github.kusumotolab.tc2p.utils.compare;

import java.util.List;
import java.util.function.Function;

public abstract class IEquivalent<T> {

  public boolean equal(final T t1, final T t2) {
    final ComparableObject<T> comparableObject = ComparableObject.create(t1, t2);
    getFunctions().forEach(comparableObject::setComparativeValue);
    return comparableObject.judge();
  }

  public int hashCode(final T t) {
    final HashCodeGenerator<T> hashCodeGenerator = HashCodeGenerator.create(t);
    getFunctions().forEach(hashCodeGenerator::setValue);
    return hashCodeGenerator.getHashCode();
  }

  public abstract List<Function<T, ?>> getFunctions();
}

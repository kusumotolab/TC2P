package com.github.kusumotolab.tc2p.utils.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class HashCodeGenerator<T> {

  private final T object;
  private final List<Function<T, ?>> functions = new ArrayList<>();

  public HashCodeGenerator(final T object) {
    this.object = object;
  }

  public static <T> HashCodeGenerator<T> create(final T object) {
    return new HashCodeGenerator<>(object);
  }

  public <U> HashCodeGenerator<T> setValue(final Function<T, U> function) {
    this.functions.add(function);
    return this;
  }

  public int getHashCode() {
    try {
      return functions.stream()
          .map(e -> e.apply(object))
          .filter(Objects::nonNull)
          .map(Object::hashCode)
          .reduce(0, Integer::sum);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return -1;
  }
}

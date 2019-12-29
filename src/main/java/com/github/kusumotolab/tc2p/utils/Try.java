package com.github.kusumotolab.tc2p.utils;

import java.util.Optional;

public class Try {

  public interface ThrowableLambda<E extends Exception> {
    void apply() throws E;
  }

  public static <E extends Exception> void lambda(final ThrowableLambda<E> lambda) {
    try {
      lambda.apply();
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException("failed force try");
    }
  }



  public interface ThrowableFunction<T, E extends Exception> {
    T apply() throws E;
  }

  public static <T, E extends Exception> T force(final ThrowableFunction<T, E> function) {
    try {
      return function.apply();
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException("failed force try");
    }
  }

  public static <T, E extends Exception> Optional<T> optional(final ThrowableFunction<T, E> function) {
    try {
      return Optional.of(function.apply());
    } catch (final Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }
}

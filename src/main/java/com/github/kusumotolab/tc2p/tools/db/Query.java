package com.github.kusumotolab.tc2p.tools.db;

public interface Query<T> {

  String toCommand();

  T resolve(final Object object);
}

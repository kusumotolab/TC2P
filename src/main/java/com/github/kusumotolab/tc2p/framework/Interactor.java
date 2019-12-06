package com.github.kusumotolab.tc2p.framework;

public interface Interactor<Input, Output> {

  Output execute(final Input input);
}

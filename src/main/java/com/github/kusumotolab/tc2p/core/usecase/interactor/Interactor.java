package com.github.kusumotolab.tc2p.core.usecase.interactor;

public interface Interactor<Input, Output> {

  Output execute(final Input input);
}

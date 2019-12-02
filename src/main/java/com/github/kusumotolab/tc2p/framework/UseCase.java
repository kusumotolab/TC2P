package com.github.kusumotolab.tc2p.framework;

public abstract class UseCase<P extends Presenter> {

  protected final P presenter;

  public UseCase(final P presenter) {
    this.presenter = presenter;
  }
}


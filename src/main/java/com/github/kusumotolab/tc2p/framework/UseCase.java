package com.github.kusumotolab.tc2p.framework;

public abstract class UseCase<Input, V extends View, P extends Presenter<V>> {

  protected final P presenter;

  public UseCase(final P presenter) {
    this.presenter = presenter;
  }

  public abstract void execute(final Input input);
}


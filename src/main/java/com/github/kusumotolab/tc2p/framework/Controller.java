package com.github.kusumotolab.tc2p.framework;


public abstract class Controller<V extends View, P extends Presenter<V>, U extends UseCase<?, V, P>> {

  protected final U useCase;

  public Controller(final U useCase) {
    this.useCase = useCase;
  }

  public abstract void exec(final String[] args);
}


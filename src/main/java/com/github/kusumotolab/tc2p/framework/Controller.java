package com.github.kusumotolab.tc2p.framework;


public abstract class Controller<P extends Presenter, U extends UseCase> {

  protected final P presenter;
  protected final U useCase;

  public Controller(final P presenter, final U useCase) {
    this.presenter = presenter;
    this.useCase = useCase;
  }

  public abstract void exec(final String args[]);
}


package com.github.kusumotolab.tc2p.framework;


public abstract class Controller<U extends UseCase> {

  protected final U useCase;

  public Controller(final U useCase) {
    this.useCase = useCase;
  }

  public abstract void exec(final String args[]);
}


package com.github.kusumotolab.tc2p.framework;

public abstract class Presenter<V extends View> {

  protected final V view;

  public Presenter(final V view) {
    this.view = view;
  }
}


package com.github.kusumotolab.tc2p.core.presenter;

import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public abstract class IMiningRepositoryPresenter<V extends View> extends Presenter<V> {

  public IMiningRepositoryPresenter(final V view) {
    super(view);
  }

  public abstract void start();

  public abstract void end();
}

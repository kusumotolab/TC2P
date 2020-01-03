package com.github.kusumotolab.tc2p.core.presenter;

import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public abstract class IViewPresenter<V extends View> extends Presenter<V> {

  public IViewPresenter(final V view) {
    super(view);
  }

  public abstract void show(final MiningResult result);

  public abstract void observeInput();
}

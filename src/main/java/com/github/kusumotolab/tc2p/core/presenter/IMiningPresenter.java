package com.github.kusumotolab.tc2p.core.presenter;

import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public abstract class IMiningPresenter<V extends View> extends Presenter<V> {

  public IMiningPresenter(final V view) {
    super(view);
  }
}

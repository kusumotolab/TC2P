package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.presenter.IViewPresenter;
import com.github.kusumotolab.tc2p.core.usecase.ViewerUseCase;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.View;

public abstract class IViewerController<V extends View, P extends IViewPresenter<V>> extends Controller<V, P, ViewerUseCase<V, P>> {

  public IViewerController(final ViewerUseCase<V, P> useCase) {
    super(useCase);
  }

  public abstract void next();

  public abstract void previous();

  public abstract void openInstance();

  public abstract void delete();

  public abstract void command(final String command);
}

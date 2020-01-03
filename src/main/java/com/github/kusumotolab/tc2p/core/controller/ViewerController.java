package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.ViewerConfiguration;
import com.github.kusumotolab.tc2p.core.presenter.IViewPresenter;
import com.github.kusumotolab.tc2p.core.usecase.ViewerUseCase;
import com.github.kusumotolab.tc2p.framework.View;

public class ViewerController<V extends View, P extends IViewPresenter<V>> extends IViewerController<V, P> {

  public ViewerController(final ViewerUseCase<V, P> useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new ViewerConfiguration(), args, useCase::execute);
  }

  @Override
  public void next() {
    useCase.next();
  }

  @Override
  public void previous() {
    useCase.previous();
  }

  @Override
  public void openInstance() {
    useCase.open();
  }

  @Override
  public void command(final String command) {
    useCase.addComment(command);
  }
}

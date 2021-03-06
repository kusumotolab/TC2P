package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.ViewerConfiguration;
import com.github.kusumotolab.tc2p.core.entities.MiningResult.UsefulState;
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
  public void delete() {
    useCase.delete();
  }

  @Override
  public void updateState(final UsefulState state) {
    useCase.updateState(state);
  }

  @Override
  public void command(final String command) {
    if (command.equals("q")) {
      useCase.finish();
      return;
    } else if (command.equals("delete")) {
      delete();
      return;
    } else if (command.isEmpty()) {
      next();
      return;
    }
    useCase.addComment(command);
  }
}

package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.BaseConfiguration;
import com.github.kusumotolab.tc2p.core.usecase.IBaseViewerUseCase;
import com.github.kusumotolab.tc2p.core.usecase.IBaseViewerUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public class BaseController <V extends View, P extends Presenter<V>, U extends IBaseViewerUseCase<V, P>> extends Controller<V, P, U> {

  public BaseController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new BaseConfiguration(), args, config -> useCase.execute(new Input(config.getDbPath())));
  }
}

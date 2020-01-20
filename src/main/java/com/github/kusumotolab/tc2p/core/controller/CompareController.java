package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.CompareConfiguration;
import com.github.kusumotolab.tc2p.core.usecase.ICompareUseCase;
import com.github.kusumotolab.tc2p.core.usecase.ICompareUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public class CompareController<V extends View, P extends Presenter<V>, U extends ICompareUseCase<V, P>> extends Controller<V, P, U> {

  public CompareController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new CompareConfiguration(), args, config -> useCase.execute(new Input(config.getBasePath(), config.getTc2pPath())));
  }
}

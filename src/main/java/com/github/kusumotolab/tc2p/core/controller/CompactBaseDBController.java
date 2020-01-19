package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.CompactBaseDBConfiguration;
import com.github.kusumotolab.tc2p.core.usecase.ICompactBaseDBUseCase;
import com.github.kusumotolab.tc2p.core.usecase.ICompactBaseDBUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public class CompactBaseDBController<V extends View, P extends Presenter<V>, U extends ICompactBaseDBUseCase<V, P>> extends
    Controller<V, P, U> {

  public CompactBaseDBController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new CompactBaseDBConfiguration(), args, config -> useCase.execute(new Input(config.getInputPath(), config.getOutputPath())));
  }
}

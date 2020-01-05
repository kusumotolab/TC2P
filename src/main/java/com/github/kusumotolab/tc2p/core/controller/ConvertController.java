package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.ConvertConfiguration;
import com.github.kusumotolab.tc2p.core.usecase.IConvertUseCase;
import com.github.kusumotolab.tc2p.core.usecase.IConvertUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public class ConvertController<V extends View, P extends Presenter<V>, U extends IConvertUseCase<V, P>> extends Controller<V, P, U> {

  public ConvertController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new ConvertConfiguration(), args, configuration -> useCase.execute(new Input(configuration.getInputFilePath(), configuration.getOutputFilePath())));
  }
}

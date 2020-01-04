package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.ConvertToJsonConfiguration;
import com.github.kusumotolab.tc2p.core.usecase.IConvertToJsonUseCase;
import com.github.kusumotolab.tc2p.core.usecase.IConvertToJsonUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public class ConvertJsonController<V extends View, P extends Presenter<V>, U extends IConvertToJsonUseCase<V, P>> extends Controller<V, P, U> {

  public ConvertJsonController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new ConvertToJsonConfiguration(), args, configuration -> {
      useCase.execute(new Input(configuration.getInputFilePath(), configuration.getOutputFilePath()));
    });
  }
}

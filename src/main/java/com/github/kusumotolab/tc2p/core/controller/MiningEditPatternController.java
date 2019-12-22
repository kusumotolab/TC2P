package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.MiningEditPatternConfiguration;
import com.github.kusumotolab.tc2p.core.presenter.IMiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.usecase.IMiningPatternUseCase.Input;
import com.github.kusumotolab.tc2p.core.usecase.MiningEditPatternUseCase;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.View;

public class MiningEditPatternController<V extends View, P extends IMiningEditPatternPresenter<V>> extends
    Controller<V, P, MiningEditPatternUseCase<V, P>> {

  public MiningEditPatternController(final MiningEditPatternUseCase<V, P> useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new MiningEditPatternConfiguration(), args, configuration -> useCase.execute(new Input(configuration.getProjectName(),
        configuration.getFrequency(), configuration.getRatio())));
  }
}

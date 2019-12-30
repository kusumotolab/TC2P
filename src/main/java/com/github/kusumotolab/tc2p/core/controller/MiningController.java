package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.MiningConfiguration;
import com.github.kusumotolab.tc2p.core.presenter.IMiningRepositoryPresenter;
import com.github.kusumotolab.tc2p.core.usecase.IMiningRepositoryUseCase;
import com.github.kusumotolab.tc2p.core.usecase.IMiningRepositoryUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.View;

public class MiningController<V extends View, P extends IMiningRepositoryPresenter<V>, U extends IMiningRepositoryUseCase<V, P>> extends
    Controller<V, P, U> {

  public MiningController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new MiningConfiguration(), args,
        configuration -> useCase.execute(new Input(configuration.getRepository(), configuration.getNumberOfCommits())));
  }
}

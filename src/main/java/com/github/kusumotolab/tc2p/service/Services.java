package com.github.kusumotolab.tc2p.service;

import com.github.kusumotolab.tc2p.core.controller.MiningController;
import com.github.kusumotolab.tc2p.core.presenter.MiningPresenter;
import com.github.kusumotolab.tc2p.core.usecase.SaveTreeNodeUseCase;
import com.github.kusumotolab.tc2p.core.view.ConsoleView;

public class Services {

  @Service(name = "preprocess")
  private static final ServiceGraph preprocess = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningPresenter::new)
      .useCase(SaveTreeNodeUseCase::new)
      .controller(MiningController::new)
      .resolve();

  private static final Services instance = new Services();

  private Services() {
  }

  static Services getInstance() {
    return instance;
  }
}

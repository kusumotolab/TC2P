package com.github.kusumotolab.tc2p.service;

import com.github.kusumotolab.tc2p.core.controller.ConvertController;
import com.github.kusumotolab.tc2p.core.controller.MiningController;
import com.github.kusumotolab.tc2p.core.controller.MiningEditPatternController;
import com.github.kusumotolab.tc2p.core.controller.ViewerController;
import com.github.kusumotolab.tc2p.core.presenter.MiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.presenter.MiningRepositoryPresenter;
import com.github.kusumotolab.tc2p.core.presenter.ViewPresenter;
import com.github.kusumotolab.tc2p.core.usecase.ConvertToJsonUseCase;
import com.github.kusumotolab.tc2p.core.usecase.ConvertToSQLiteUseCase;
import com.github.kusumotolab.tc2p.core.usecase.MiningEditPatternUseCase;
import com.github.kusumotolab.tc2p.core.usecase.SaveTreeNodeRepositoryUseCase;
import com.github.kusumotolab.tc2p.core.usecase.ViewerUseCase;
import com.github.kusumotolab.tc2p.core.view.ConsoleView;
import com.github.kusumotolab.tc2p.core.view.InteractiveConsoleView;

public class Services {

  @Service(name = "preprocess")
  private static final ServiceGraph<?, ?, ?, ?> preprocess = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningRepositoryPresenter::new)
      .useCase(SaveTreeNodeRepositoryUseCase::new)
      .controller(MiningController::new)
      .resolve();

  @Service(name = "mining")
  private static final ServiceGraph<?, ?, ?, ?> mining = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(MiningEditPatternUseCase::new)
      .controller(MiningEditPatternController::new)
      .resolve();

  @Service(name = "view")
  private static final ServiceGraph<?, ?, ?, ?> view;

  @Service(name = "convert-json")
  private static final ServiceGraph<?, ?, ?, ?> convert_json = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(ConvertToJsonUseCase::new)
      .controller(ConvertController::new)
      .resolve();

  @Service(name = "convert-sqlite")
  private static final ServiceGraph<?, ?, ?, ?> convert_sqlite = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(ConvertToSQLiteUseCase::new)
      .controller(ConvertController::new)
      .resolve();

  static {
    final InteractiveConsoleView consoleView = new InteractiveConsoleView();
    view = ServiceGraph.view(() -> consoleView)
        .presenter(ViewPresenter::new)
        .useCase(ViewerUseCase::new)
        .controller(e -> {
          final ViewerController<InteractiveConsoleView, ViewPresenter> controller = new ViewerController<>(e);
          consoleView.setController(controller);
          return controller;
        })
        .resolve();
  }

  private static final Services instance = new Services();

  private Services() {
  }

  static Services getInstance() {
    return instance;
  }
}

package com.github.kusumotolab.tc2p.service;

import com.github.kusumotolab.tc2p.core.controller.CompactBaseDBController;
import com.github.kusumotolab.tc2p.core.controller.CompareController;
import com.github.kusumotolab.tc2p.core.controller.ConvertController;
import com.github.kusumotolab.tc2p.core.controller.CountController;
import com.github.kusumotolab.tc2p.core.controller.DomainDBCreatorController;
import com.github.kusumotolab.tc2p.core.controller.MiningController;
import com.github.kusumotolab.tc2p.core.controller.MiningEditPatternController;
import com.github.kusumotolab.tc2p.core.controller.ViewerController;
import com.github.kusumotolab.tc2p.core.presenter.MiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.presenter.MiningRepositoryPresenter;
import com.github.kusumotolab.tc2p.core.presenter.ViewPresenter;
import com.github.kusumotolab.tc2p.core.usecase.BaseUseCase;
import com.github.kusumotolab.tc2p.core.usecase.CompactBaseDBUseCase;
import com.github.kusumotolab.tc2p.core.usecase.CompareUseCase;
import com.github.kusumotolab.tc2p.core.usecase.ConvertToJsonUseCase;
import com.github.kusumotolab.tc2p.core.usecase.ConvertToSQLiteUseCase;
import com.github.kusumotolab.tc2p.core.usecase.CountUsefulUseCase;
import com.github.kusumotolab.tc2p.core.usecase.DomainDBCreateUseCase;
import com.github.kusumotolab.tc2p.core.usecase.MiningEditPatternUseCase;
import com.github.kusumotolab.tc2p.core.usecase.RxBaseUseCase;
import com.github.kusumotolab.tc2p.core.usecase.RxMiningEditPatternUseCase;
import com.github.kusumotolab.tc2p.core.usecase.SaveTreeNodeRepositoryUseCase;
import com.github.kusumotolab.tc2p.core.usecase.ViewerUseCase;
import com.github.kusumotolab.tc2p.core.view.ConsoleView;
import com.github.kusumotolab.tc2p.core.view.InteractiveConsoleView;
import com.github.kusumotolab.tc2p.utils.patternmining.MultipleParallelFreqt;
import com.github.kusumotolab.tc2p.utils.patternmining.ParallelFreqt;
import com.github.kusumotolab.tc2p.utils.patternmining.RxMultipleFreqt;

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
      .useCase(presenter -> new MiningEditPatternUseCase<>(presenter, new ParallelFreqt()))
      .controller(MiningEditPatternController::new)
      .resolve();

  @Service(name = "rx-mining")
  private static final ServiceGraph<?, ?, ?, ?> rxMining = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(RxMiningEditPatternUseCase::new)
      .controller(MiningEditPatternController::new)
      .resolve();

  @Service(name = "rx-mining-domain")
  private static final ServiceGraph<?, ?, ?, ?> rxMiningDomain = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(e -> new RxMiningEditPatternUseCase<>(e, new RxMultipleFreqt()))
      .controller(MiningEditPatternController::new)
      .resolve();


  @Service(name = "mining-domain")
  private static final ServiceGraph<?, ?, ?, ?> miningDomain = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(presenter -> new MiningEditPatternUseCase<>(presenter, new MultipleParallelFreqt()))
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

  @Service(name = "integrate")
  private static final ServiceGraph<?, ?, ?, ?> integrate = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(DomainDBCreateUseCase::new)
      .controller(DomainDBCreatorController::new)
      .resolve();

  @Service(name = "count")
  private static final ServiceGraph<?, ?, ?, ?> count = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(CountUsefulUseCase::new)
      .controller(CountController::new)
      .resolve();

  @Service(name = "base")
  private static final ServiceGraph<?, ?, ?, ?> base = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(BaseUseCase::new)
      .controller(MiningEditPatternController::new)
      .resolve();

  @Service(name = "rx-base")
  private static final ServiceGraph<?, ?, ?, ?> rx_base = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(RxBaseUseCase::new)
      .controller(MiningEditPatternController::new)
      .resolve();

  @Service(name = "compact")
  private static final ServiceGraph<?, ?, ?, ?> compact = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(CompactBaseDBUseCase::new)
      .controller(CompactBaseDBController::new)
      .resolve();

  @Service(name = "compare")
  private static final ServiceGraph<?, ?, ?, ?> compare = ServiceGraph.view(ConsoleView::new)
      .presenter(MiningEditPatternPresenter::new)
      .useCase(CompareUseCase::new)
      .controller(CompareController::new)
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

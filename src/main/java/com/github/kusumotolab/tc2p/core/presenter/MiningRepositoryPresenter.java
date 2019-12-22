package com.github.kusumotolab.tc2p.core.presenter;

import com.github.kusumotolab.tc2p.core.view.ConsoleView;

public class MiningRepositoryPresenter extends IMiningRepositoryPresenter<ConsoleView> {

  public MiningRepositoryPresenter(final ConsoleView view) {
    super(view);
  }

  @Override
  public void start() {
    view.print("Start");
  }

  @Override
  public void end() {
    view.print("End");
  }
}

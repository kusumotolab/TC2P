package com.github.kusumotolab.tc2p.core.presenter;

import com.github.kusumotolab.tc2p.core.view.ConsoleView;

public class MiningPresenter extends IMiningPresenter<ConsoleView> {

  public MiningPresenter(final ConsoleView view) {
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

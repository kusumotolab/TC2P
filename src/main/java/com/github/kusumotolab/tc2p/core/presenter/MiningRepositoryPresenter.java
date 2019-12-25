package com.github.kusumotolab.tc2p.core.presenter;

import java.time.Duration;
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

  @Override
  public void time(final String name, final Duration duration) {
    view.print(name + ": " + duration.getSeconds() + "(s)");
  }
}

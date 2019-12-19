package com.github.kusumotolab.tc2p.core.controller;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import com.github.kusumotolab.tc2p.core.configuration.MiningConfiguration;
import com.github.kusumotolab.tc2p.core.presenter.IMiningPresenter;
import com.github.kusumotolab.tc2p.core.usecase.IMiningUseCase;
import com.github.kusumotolab.tc2p.core.usecase.IMiningUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.View;

public class MiningController<V extends View, P extends IMiningPresenter<V>, U extends IMiningUseCase<V, P>>  extends Controller<V, P, U> {

  public MiningController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    final MiningConfiguration configuration = new MiningConfiguration();
    final CmdLineParser parser = new CmdLineParser(configuration);
    try {
      parser.parseArgument(args);
      useCase.execute(new Input(configuration.getRepository()));
    } catch (final CmdLineException e) {
      parser.printUsage(System.err);
    }
  }
}

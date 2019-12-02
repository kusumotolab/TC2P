package com.github.kusumotolab.tc2p.core.controller;

import java.nio.file.Paths;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import com.github.kusumotolab.tc2p.core.configuration.MiningConfiguration;
import com.github.kusumotolab.tc2p.core.presenter.IMiningPresenter;
import com.github.kusumotolab.tc2p.core.usecase.IMiningUseCase;
import com.github.kusumotolab.tc2p.core.usecase.IMiningUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;

public class MiningController<P extends IMiningPresenter>  extends Controller<IMiningUseCase<P>> {

  public MiningController(final IMiningUseCase<P> useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    final MiningConfiguration configuration = new MiningConfiguration();
    final CmdLineParser parser = new CmdLineParser(configuration);
    try {
      parser.parseArgument(args);
      useCase.execute(new Input(Paths.get(configuration.getRepository())));
    } catch (final CmdLineException e) {
      e.printStackTrace();
      parser.printUsage(System.err);
    }
  }
}

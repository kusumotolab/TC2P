package com.github.kusumotolab.tc2p.framework;


import java.util.function.Consumer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public abstract class Controller<V extends View, P extends Presenter<V>, U extends UseCase<?, V, P>> {

  protected final U useCase;

  public Controller(final U useCase) {
    this.useCase = useCase;
  }

  public abstract void exec(final String[] args);

  protected  <Configuration> void parse(final Configuration configuration, final String[] args,
      final Consumer<Configuration> consumer) {
    final CmdLineParser parser = new CmdLineParser(configuration);
    try {
      parser.parseArgument(args);
      consumer.accept(configuration);
    } catch (final CmdLineException e) {
      parser.printUsage(System.err);
    }
  }
}


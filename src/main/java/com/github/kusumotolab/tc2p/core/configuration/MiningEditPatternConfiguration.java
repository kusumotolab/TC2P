package com.github.kusumotolab.tc2p.core.configuration;

import org.kohsuke.args4j.Option;
import lombok.Getter;

public class MiningEditPatternConfiguration {

  @Getter
  @Option(name = "-p", aliases = "--project", usage = "the project name", required = true)
  private String projectName;

  @Getter
  @Option(name = "-f", aliases = "--frequency", usage = "the frequency of patterns", required = true)
  private int frequency;

  @Getter
  @Option(name = "-r", aliases = "--ratio", usage = "the ration of tree nodes")
  private double ratio;
}

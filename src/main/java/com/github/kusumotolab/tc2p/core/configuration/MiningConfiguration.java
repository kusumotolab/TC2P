package com.github.kusumotolab.tc2p.core.configuration;

import org.kohsuke.args4j.Option;

public class MiningConfiguration {

  @Option(name = "-r", aliases = "the repository path (including .git)")
  private String repository;

  public String getRepository() {
    return repository;
  }
}

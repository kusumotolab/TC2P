package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import lombok.Getter;

public class MiningConfiguration {

  @Getter
  @Option(name = "-r", aliases = "--repository", usage = "the repository path", handler = PathOptionHandler.class, required = true)
  private Path repository;
}

package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import lombok.Getter;

public class BaseConfiguration {

  @Getter
  @Option(name = "-d", handler = PathOptionHandler.class)
  private Path dbPath;
}

package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import lombok.Getter;

public class DomainDBCreateConfiguration {

  @Getter
  @Option(name = "-i", aliases = "--input", usage = "json file", required = true, handler = PathOptionHandler.class)
  private Path jsonPath;
}

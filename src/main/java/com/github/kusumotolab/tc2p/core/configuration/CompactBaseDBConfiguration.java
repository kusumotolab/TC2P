package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import lombok.Getter;

public class CompactBaseDBConfiguration {

  @Getter
  @Option(name = "-i", aliases = "--input", usage = "the input sqlite path", handler = PathOptionHandler.class)
  private Path inputPath;

  @Getter
  @Option(name = "-o", aliases = "--output", usage = "the output sqlite path", handler = PathOptionHandler.class)
  private Path outputPath;

}

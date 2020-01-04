package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import lombok.Getter;

public class ConvertToJsonConfiguration {

  @Getter
  @Option(name = "-i", aliases = "--input", usage = "the input file (txt)", required = true, handler = PathOptionHandler.class)
  private Path inputFilePath;


  @Getter
  @Option(name = "-o", aliases = "--output", usage = "the output path (json)", required = true, handler = PathOptionHandler.class)
  private Path outputFilePath;
}

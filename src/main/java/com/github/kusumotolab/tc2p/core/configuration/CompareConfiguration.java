package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import lombok.Getter;

public class CompareConfiguration {

  @Getter
  @Option(name = "-b", aliases = "--base", usage = "The Path of SQLite which stored Base Results", handler = PathOptionHandler.class)
  private Path basePath;

  @Getter
  @Option(name = "-t", aliases = "--tc2p", usage = "The Path of SQLite which stored TC2P Results", handler = PathOptionHandler.class)
  private Path tc2pPath;

}

package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import lombok.Getter;

public class ViewerConfiguration {

  @Getter
  @Option(name = "-i", aliases = "--input", usage = "the results file (json)", required = true, handler = PathOptionHandler.class)
  private Path inputFilePath;

  @Getter
  @Option(name = "-s", aliases = "--sort", usage = "pattern are sorted by this property")
  private SortElement sort = SortElement.INDEX;

  @Getter
  @Option(name = "-r", aliases = "--reverse", usage = "reverse the order")
  private boolean reverse = false;

  @Getter
  @Option(name = "-a", aliases = "--show-all", usage = "show all pattern")
  private boolean showAll = false;

  @Getter
  @Option(name = "-c", aliases = "--show-commented", usage = "show only commented")
  private boolean showOnlyCommented = false;

  public enum SortElement {
    INDEX, SIZE, DEPTH, FREQUENCY
  }
}

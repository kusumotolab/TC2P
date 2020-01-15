package com.github.kusumotolab.tc2p.core.configuration;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import com.github.kusumotolab.tc2p.core.entities.Tag;
import com.google.common.collect.Lists;
import lombok.Getter;

public class CountUsefulConfiguration {

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
  @Option(name = "-c", aliases = "--count", usage = "the number of patterns")
  private int count = 50;

  @Getter
  private List<Tag> tags = Lists.newArrayList();

  @Option(name = "-t", aliases = "--tag", usage = "search patterns which have the tag (For, Enhanced_For, While, Try, Switch)", handler = StringArrayOptionHandler.class)
  public void addTag(final String text) {
    final boolean containsTag = Arrays.stream(Tag.values()).anyMatch(tag -> tag.toString().equals(text.toUpperCase()));
    if (!containsTag) {
      throw new RuntimeException("The tag (" + text + ") dose not exist.");
    }
    tags.add(Tag.valueOf(text.toUpperCase()));
  }

  public enum SortElement {
    INDEX, NODE_SIZE, ACTION_SIZE, DEPTH, FREQUENCY
  }
}

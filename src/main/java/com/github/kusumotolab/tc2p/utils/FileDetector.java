package com.github.kusumotolab.tc2p.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileDetector {

  public static Stream<Path> execute(final Path path) {
    try {
      return Files.walk(path, Integer.MAX_VALUE);
    } catch (final IOException e) {
      return Stream.empty();
    }
  }
}

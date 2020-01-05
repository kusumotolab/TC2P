package com.github.kusumotolab.tc2p.utils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.google.common.collect.Lists;

public class FileUtil {

  public static void overWrite(final Path path, final String text) {
    Try.lambda(() -> {
      if (Files.exists(path)) {
        Files.delete(path);
      }
      Files.createFile(path);
      Files.write(path, Lists.newArrayList(text), Charset.defaultCharset(), StandardOpenOption.TRUNCATE_EXISTING);
    });
  }
}

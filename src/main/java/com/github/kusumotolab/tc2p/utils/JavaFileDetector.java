package com.github.kusumotolab.tc2p.utils;

import java.nio.file.Path;
import java.util.stream.Stream;
import io.reactivex.Observable;

public class JavaFileDetector {

  public static final Rx rx = new Rx();

  public static Stream<Path> execute(final Path path) {
    return FileDetector.execute(path)
        .filter(e -> e.toFile().isFile())
        .filter(e -> e.toString()
            .endsWith("java"));
  }


  public static class Rx {

    private Rx() {}

    public Observable<Path> execute(final Path path) {
      return Observable.fromIterable(() -> JavaFileDetector.execute(path)
          .iterator());
    }
  }
}


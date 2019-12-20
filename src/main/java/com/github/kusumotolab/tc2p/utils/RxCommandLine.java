package com.github.kusumotolab.tc2p.utils;

import java.io.File;
import com.github.kusumotolab.sdl4j.util.CommandLine;
import com.github.kusumotolab.sdl4j.util.CommandLine.CommandLineResult;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RxCommandLine {

  private final CommandLine commandLine = new CommandLine();
  private final File dir;

  public RxCommandLine() {
    this(new File("."));
  }

  public RxCommandLine(final File dir) {
    this.dir = dir;
  }

  public Single<CommandLineResult> execute(final String... command) {
    log.debug(String.join(" ", command));
    return Single.create(emitter -> {
      final CommandLineResult result = this.commandLine.execute(dir, command);
      emitter.onSuccess(result);
    });
  }
}

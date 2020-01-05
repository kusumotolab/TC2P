package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.usecase.IConvertUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;
import lombok.Data;

public abstract class IConvertUseCase<V extends View, P extends Presenter<V>> extends UseCase<Input, V, P> {

  public IConvertUseCase(final P presenter) {
    super(presenter);
  }

  @Data
  public static class Input {
    private final Path inputPath;
    private final Path outputPath;
  }
}

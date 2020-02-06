package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.usecase.IBaseViewerUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;
import lombok.Data;

public abstract class IBaseViewerUseCase<V extends View, P extends Presenter<V>> extends UseCase<Input, V, P> {

  public IBaseViewerUseCase(final P presenter) {
    super(presenter);
  }

  @Data
  public static class Input {
    private final Path dbPath;
  }
}

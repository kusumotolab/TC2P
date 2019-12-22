package com.github.kusumotolab.tc2p.core.usecase;

import com.github.kusumotolab.tc2p.core.usecase.IMiningPatternUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;
import lombok.Data;

public abstract class IMiningPatternUseCase<V extends View, P extends Presenter<V>> extends UseCase<Input, V, P> {

  public IMiningPatternUseCase(final P presenter) {
    super(presenter);
  }

  @Data
  public static class Input {
    private final String projectName;
    private final int frequency;
    private final double ratio;
  }
}


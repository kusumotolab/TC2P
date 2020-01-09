package com.github.kusumotolab.tc2p.core.usecase;

import java.util.List;
import com.github.kusumotolab.tc2p.core.usecase.IDomainDBCreateUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;
import lombok.Data;

public abstract class IDomainDBCreateUseCase<V extends View,   P extends Presenter<V>> extends UseCase<Input, V, P> {


  public IDomainDBCreateUseCase(final P presenter) {
    super(presenter);
  }

  @Data
  public static class Input {
    private final String domain;
    private final List<String> projects;
  }
}

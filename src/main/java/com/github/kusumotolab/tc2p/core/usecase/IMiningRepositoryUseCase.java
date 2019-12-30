package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.presenter.IMiningRepositoryPresenter;
import com.github.kusumotolab.tc2p.core.usecase.IMiningRepositoryUseCase.Input;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;
import lombok.Data;

public abstract class IMiningRepositoryUseCase<V extends View, P extends IMiningRepositoryPresenter<V>> extends UseCase<Input, V, P> {

  public IMiningRepositoryUseCase(final P presenter) {
    super(presenter);
  }

  @Data
  public static class Input {
    private final Path repositoryPath;
    private final long numberOfCommits;
  }
}

package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.presenter.IMiningPresenter;
import com.github.kusumotolab.tc2p.core.usecase.IMiningUseCase.Input;
import com.github.kusumotolab.tc2p.framework.UseCase;

public abstract class IMiningUseCase<P extends IMiningPresenter<?>> extends UseCase<Input, P> {

  public IMiningUseCase(final P presenter) {
    super(presenter);
  }


  public static class Input {

    private final Path repositoryPath;

    public Input(final Path repositoryPath) {
      this.repositoryPath = repositoryPath;
    }

    public Path getRepositoryPath() {
      return repositoryPath;
    }
  }
}

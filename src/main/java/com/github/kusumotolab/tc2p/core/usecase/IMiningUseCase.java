package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.presenter.IMiningPresenter;
import com.github.kusumotolab.tc2p.core.usecase.IMiningUseCase.Input;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;

public abstract class IMiningUseCase<V extends View, P extends IMiningPresenter<V>> extends UseCase<Input, V, P> {

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

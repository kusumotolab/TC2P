package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.presenter.IMiningPresenter;

public class SaveTreeNodeUseCase<P extends IMiningPresenter> extends IMiningUseCase<P> {

  public SaveTreeNodeUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Path repositoryPath = input.getRepositoryPath();
    System.out.println(repositoryPath.toAbsolutePath());
  }
}

package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import java.util.Optional;
import com.github.kusumotolab.tc2p.core.entities.CommitLogPair;
import com.github.kusumotolab.tc2p.core.entities.FileHistory;
import com.github.kusumotolab.tc2p.core.presenter.IMiningPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.GumTreeExecutor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningInteractor;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeOutput;
import io.reactivex.Observable;

public class SaveTreeNodeUseCase<P extends IMiningPresenter<?>> extends IMiningUseCase<P> {

  public SaveTreeNodeUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Path path = input.getRepositoryPath();
    final Observable<FileHistory> fileHistories = new MiningInteractor().execute(path);
    final Observable<CommitLogPair> commitPairs = fileHistories.map(FileHistory::trace)
        .flatMap(Observable::fromIterable);

    final GumTreeExecutor gumTreeExecutor = new GumTreeExecutor(input.getRepositoryPath());
    final Observable<GumTreeOutput> gumTreeOutputs = commitPairs.map(gumTreeExecutor::execute)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }
}

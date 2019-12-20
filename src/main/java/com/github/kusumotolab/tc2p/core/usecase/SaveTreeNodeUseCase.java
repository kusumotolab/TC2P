package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import java.util.Optional;
import com.github.kusumotolab.tc2p.core.entities.CommitLogPair;
import com.github.kusumotolab.tc2p.core.entities.FileHistory;
import com.github.kusumotolab.tc2p.core.presenter.IMiningPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.GumTreeExecutor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningInteractor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.SaveEditScriptInteractor;
import com.github.kusumotolab.tc2p.framework.View;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class SaveTreeNodeUseCase<V extends View, P extends IMiningPresenter<V>> extends
    IMiningUseCase<V, P> {

  public SaveTreeNodeUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Path path = input.getRepositoryPath();
    final String projectName = path.getFileName()
        .toString();
    final Observable<FileHistory> fileHistories = new MiningInteractor().execute(path);
    final Observable<CommitLogPair> commitPairs = fileHistories.map(FileHistory::trace)
        .flatMap(Observable::fromIterable);

    final GumTreeExecutor gumTreeExecutor = new GumTreeExecutor(input.getRepositoryPath());
    final Observable<SaveEditScriptInteractor.Input> inputObservable = commitPairs.map(
        pair -> gumTreeExecutor.execute(pair)
            .map(output -> new SaveEditScriptInteractor.Input(projectName, pair, output)))
        .filter(Optional::isPresent)
        .map(Optional::get);

    presenter.start();
    final Disposable disposable = new SaveEditScriptInteractor().execute(inputObservable)
        .subscribe(presenter::end);
    disposable.dispose();
  }
}

package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import java.util.Optional;
import com.github.kusumotolab.tc2p.core.entities.CommitLogPair;
import com.github.kusumotolab.tc2p.core.entities.FileHistory;
import com.github.kusumotolab.tc2p.core.presenter.IMiningRepositoryPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.GumTreeExecutor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningRepositoryInteractor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.SaveEditScriptInteractor;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.JavaFileDetector;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class SaveTreeNodeRepositoryUseCase<V extends View, P extends IMiningRepositoryPresenter<V>> extends
    IMiningRepositoryUseCase<V, P> {

  public SaveTreeNodeRepositoryUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Path path = input.getRepositoryPath();
    final String projectName = path.getFileName().toString();
    JavaFileDetector.rx.execute(input.getRepositoryPath()).flatMapCompletable(filePath -> {
      final MiningRepositoryInteractor.Input miningInput = new MiningRepositoryInteractor.Input(input.getRepositoryPath(), filePath);
      final Observable<FileHistory> fileHistories = new MiningRepositoryInteractor().execute(Observable.just(miningInput));
      final Observable<CommitLogPair> commitPairs = fileHistories.map(FileHistory::trace)
          .flatMap(Observable::fromIterable);

      final GumTreeExecutor gumTreeExecutor = new GumTreeExecutor(input.getRepositoryPath());
      final Observable<SaveEditScriptInteractor.Input> inputObservable = commitPairs.map(pair -> gumTreeExecutor.execute(pair)
          .map(output -> new SaveEditScriptInteractor.Input(projectName, pair, output)))
          .filter(Optional::isPresent)
          .map(Optional::get);

      presenter.start();
      return new SaveEditScriptInteractor().execute(inputObservable)
          .subscribeOn(Schedulers.computation());
    }).subscribeOn(Schedulers.computation()).blockingAwait();
    presenter.end();
  }
}

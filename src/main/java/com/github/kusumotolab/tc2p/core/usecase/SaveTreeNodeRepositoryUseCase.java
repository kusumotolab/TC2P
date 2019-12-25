package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.entities.CommitPair;
import com.github.kusumotolab.tc2p.core.presenter.IMiningRepositoryPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.GumTreeExecutor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningRepositoryInteractor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.SaveEditScriptInteractor;
import com.github.kusumotolab.tc2p.framework.View;
import com.google.common.base.Stopwatch;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveTreeNodeRepositoryUseCase<V extends View, P extends IMiningRepositoryPresenter<V>> extends
    IMiningRepositoryUseCase<V, P> {

  public SaveTreeNodeRepositoryUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Path path = input.getRepositoryPath();
    final String projectName = path.getFileName().toString();
    final MiningRepositoryInteractor.Input miningInput = new MiningRepositoryInteractor.Input(input.getRepositoryPath(), "master");
    final Observable<CommitPair> commitPairs = new MiningRepositoryInteractor().execute(Observable.just(miningInput));

    final GumTreeExecutor gumTreeExecutor = new GumTreeExecutor(input.getRepositoryPath());
    final Observable<SaveEditScriptInteractor.Input> inputObservable = commitPairs
        .flatMap(pair -> Observable.just(pair).subscribeOn(Schedulers.computation()))
        .flatMap(pair -> gumTreeExecutor.execute(pair)
            .subscribeOn(Schedulers.computation())
            .map(output -> new SaveEditScriptInteractor.Input(projectName, pair, output))
        );

    presenter.start();
    final Stopwatch stopwatch = Stopwatch.createStarted();
    new SaveEditScriptInteractor().execute(inputObservable)
        .blockingAwait();
    presenter.end();
    presenter.time("Save Edit Script", stopwatch.elapsed());
  }
}

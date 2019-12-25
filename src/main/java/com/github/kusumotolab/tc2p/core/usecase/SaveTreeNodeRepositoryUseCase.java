package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.entities.CommitPair;
import com.github.kusumotolab.tc2p.core.presenter.IMiningRepositoryPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.GumTreeExecutor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningRepositoryInteractor;
import com.github.kusumotolab.tc2p.core.usecase.interactor.SaveEditScriptInteractor;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeOutput;
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
    final MiningRepositoryInteractor.Input miningInput = new MiningRepositoryInteractor.Input(input.getRepositoryPath(), "master");
    final Observable<CommitPair> commitPairs = new MiningRepositoryInteractor().execute(Observable.just(miningInput));

    final GumTreeExecutor gumTreeExecutor = new GumTreeExecutor(input.getRepositoryPath());
    final Observable<SaveEditScriptInteractor.Input> inputObservable = commitPairs.flatMap(pair -> {
      final Observable<GumTreeOutput> outputs = gumTreeExecutor.execute(pair);
      return outputs.map(output -> new SaveEditScriptInteractor.Input(projectName, pair, output))
          .subscribeOn(Schedulers.computation());
    });

    presenter.start();
    new SaveEditScriptInteractor().execute(inputObservable)
        .subscribeOn(Schedulers.computation())
        .blockingAwait();
    presenter.end();
  }
}

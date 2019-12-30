package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.nio.file.Path;
import java.util.Optional;
import com.github.kusumotolab.tc2p.core.entities.CommitPair;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningRepositoryInteractor.Input;
import com.github.kusumotolab.tc2p.framework.Interactor;
import com.github.kusumotolab.tc2p.tools.git.GitClient;
import io.reactivex.Observable;
import lombok.Data;

public class MiningRepositoryInteractor implements Interactor<Observable<Input>, Observable<CommitPair>> {

  @Override
  public Observable<CommitPair> execute(final Observable<Input> observable) {
    return observable.flatMap(input -> {
      final Optional<GitClient> optionalGitClient = GitClient.create(input.getRepositoryPath());
      if (!optionalGitClient.isPresent()) {
        return Observable.empty();
      }
      final GitClient gitClient = optionalGitClient.get();
      return gitClient.log();
    }).scan(new CommitPair(null, null), (pair, revCommit) -> new CommitPair(revCommit, pair.getSrcCommit()))
        .filter(e -> e.getDstCommit() != null && e.getSrcCommit() != null);
  }

  @Data
  public static class Input {
    private final Path repositoryPath;
  }
}

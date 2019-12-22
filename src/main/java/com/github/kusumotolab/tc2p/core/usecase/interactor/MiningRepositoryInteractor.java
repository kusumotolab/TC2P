package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.nio.file.Path;
import java.util.Optional;
import com.github.kusumotolab.tc2p.core.entities.FileHistory;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningRepositoryInteractor.Input;
import com.github.kusumotolab.tc2p.framework.Interactor;
import com.github.kusumotolab.tc2p.tools.git.GitClient;
import io.reactivex.Observable;
import lombok.Data;

public class MiningRepositoryInteractor implements Interactor<Observable<Input>, Observable<FileHistory>> {

  @Override
  public Observable<FileHistory> execute(final Observable<Input> observable) {
    return observable.flatMap(input -> {
      final Optional<GitClient> optionalGitClient = GitClient.create(input.getRepositoryPath());
      if (optionalGitClient.isEmpty()) {
        return Observable.empty();
      }
      final GitClient gitClient = optionalGitClient.get();
      return gitClient.logWithFollow(input.getPath())
          .toList()
          .map(FileHistory::new)
          .toObservable();
    });
  }

  @Data
  public static class Input {
    private final Path repositoryPath;
    private final Path path;
  }
}

package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.nio.file.Path;
import java.util.Optional;
import com.github.kusumotolab.tc2p.core.entities.FileHistory;
import com.github.kusumotolab.tc2p.framework.Interactor;
import com.github.kusumotolab.tc2p.tools.git.GitClient;
import com.github.kusumotolab.tc2p.utils.JavaFileDetector;
import io.reactivex.Observable;
import io.reactivex.Single;

public class MiningInteractor implements Interactor<Path, Observable<FileHistory>> {

  @Override
  public Observable<FileHistory> execute(final Path path) {
    final Optional<GitClient> optionalGitClient = GitClient.create(path);
    if (optionalGitClient.isEmpty()) {
      return Observable.empty();
    }
    final GitClient gitClient = optionalGitClient.get();

    return JavaFileDetector.rx.execute(path)
        .map(gitClient::logWithFollow)
        .map(Observable::toList)
        .map(Single::blockingGet)
        .map(FileHistory::new);
  }
}

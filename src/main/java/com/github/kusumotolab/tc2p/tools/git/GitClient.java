package com.github.kusumotolab.tc2p.tools.git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import com.github.kusumotolab.tc2p.core.entities.CommitPair;
import com.github.kusumotolab.tc2p.core.entities.FileRevision;
import com.github.kusumotolab.tc2p.tools.git.GitCatBlob.GitBlobInput;
import com.github.kusumotolab.tc2p.utils.Try;
import io.reactivex.Maybe;
import io.reactivex.Observable;

public class GitClient {

  private final Repository repository;

  private GitClient(final Path gitPath) throws IOException {
    this.repository = FileRepositoryBuilder.create(gitPath.toFile());
  }

  public static Optional<GitClient> create(final Path path) {
    return Try.optional(() -> new GitClient(path.resolve(".git")));
  }

  public Observable<RevCommit> log(final String id) {
    return new GitLog(repository).execute(id);
  }

  public Observable<CommitLog> logWithFollow(final Path filePath) {
    return new GitLogWithFollow(repository).execute(filePath);
  }

  public Observable<FileRevision> show(final CommitPair commitPair) {
    return new GitShowDiff(repository).execute(commitPair);
  }

  public Maybe<String> catBlob(final RevCommit commit, final String path) {
    return new GitCatBlob(repository).execute(new GitBlobInput(path, commit));
  }
}

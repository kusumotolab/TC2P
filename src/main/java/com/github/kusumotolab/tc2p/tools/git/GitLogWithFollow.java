package com.github.kusumotolab.tc2p.tools.git;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import com.github.kusumotolab.sdl4j.util.CommandLine.CommandLineResult;
import com.github.kusumotolab.tc2p.utils.RxCommandLine;
import io.reactivex.Observable;

public class GitLogWithFollow extends GitCommand<Path, Observable<CommitLog>> {

  public GitLogWithFollow(final Repository repository) {
    super(repository);
  }

  @Override
  public Observable<CommitLog> execute(final Path path) {
    return new RxCommandLine(repository.getDirectory())
        .execute("git", "log", "--pretty=oneline", "--follow", "--name-only", path.toString())
        .map(CommandLineResult::getOutputLines)
        .flatMapObservable(Observable::fromIterable)
        .buffer(2) // name-only 対策
        .map(e -> {
          final String commitId = e.get(0)
              .split(" ")[0];
          final String filepath = e.get(1);
          final RevCommit commit = repository.parseCommit(ObjectId.fromString(commitId));
          return new CommitLog(commit, Paths.get(filepath));
        });
  }
}

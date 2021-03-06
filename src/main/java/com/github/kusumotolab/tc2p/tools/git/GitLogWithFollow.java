package com.github.kusumotolab.tc2p.tools.git;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import com.github.kusumotolab.sdl4j.util.CommandLine.CommandLineResult;
import com.github.kusumotolab.tc2p.utils.RxCommandLine;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitLogWithFollow extends GitCommand<Path, Observable<CommitLog>> {

  public GitLogWithFollow(final Repository repository) {
    super(repository);
  }

  @Override
  public Observable<CommitLog> execute(final Path path) {
    final Path gitRootPath = repository.getDirectory().toPath().getParent();
    final Path relativePath = gitRootPath.toAbsolutePath()
        .relativize(path.toAbsolutePath());
    return new RxCommandLine(gitRootPath.toFile())
        .execute("git", "log", "--pretty=oneline", "--follow", "--name-only",
            relativePath.toString())
        .map(CommandLineResult::getOutputLines)
        .doOnSuccess(texts -> texts.forEach(log::debug))
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

package com.github.kusumotolab.tc2p.tools.gumtree;

import java.nio.file.Path;
import java.util.Optional;
import org.eclipse.jgit.revwalk.RevCommit;
import com.github.kusumotolab.tc2p.core.entities.CommitLogPair;
import com.github.kusumotolab.tc2p.tools.git.CommitLog;
import com.github.kusumotolab.tc2p.tools.git.GitClient;
import io.reactivex.Maybe;

public class GumTreeAdapter {

  private final Path repositoryPath;

  public GumTreeAdapter(final Path repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  public Optional<GumTreeInput> convert(final CommitLogPair commitLogPair) {
    return GitClient.create(repositoryPath)
        .map(gitClient -> {
          final CommitLog srcCommitLog = commitLogPair.getSrcCommitLog();
          final CommitLog dstCommitLog = commitLogPair.getDstCommitLog();

          final Maybe<String> srcContents = getContents(srcCommitLog, gitClient);
          final Maybe<String> dstContents = getContents(dstCommitLog, gitClient);

          return createGumTreeInput(srcCommitLog.getFileName(), dstCommitLog.getFileName(),
              srcContents.blockingGet(), dstContents.blockingGet());
        });
  }

  private Maybe<String> getContents(final CommitLog commitLog, final GitClient gitClient) {
    final RevCommit commit = commitLog.getCommit();
    final String filePath = commitLog.getFileName()
        .toString();

    return gitClient.catBlob(commit, filePath)
        .map(content ->
            filePath.endsWith(".java") ? content
                : filePath.endsWith(".mjava") ? "class XXX { " + content + "}" : "class XXX {}");
  }

  private GumTreeInput createGumTreeInput(final Path srcPath, final Path dstPath,
      final String srcContents, final String dstContents) {
    return new GumTreeInput(srcPath.toString(), dstPath.toString(), srcContents, dstContents);
  }
}

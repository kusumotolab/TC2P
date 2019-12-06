package com.github.kusumotolab.tc2p.tools.git;

import java.nio.file.Path;
import org.eclipse.jgit.revwalk.RevCommit;

public class CommitLog {

  private final RevCommit commit;
  private final Path fileName;

  public CommitLog(final RevCommit commit, final Path fileName) {
    this.commit = commit;
    this.fileName = fileName;
  }

  public RevCommit getCommit() {
    return commit;
  }

  public Path getFileName() {
    return fileName;
  }
}

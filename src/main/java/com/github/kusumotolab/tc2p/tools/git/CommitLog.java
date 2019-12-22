package com.github.kusumotolab.tc2p.tools.git;

import java.nio.file.Path;
import org.eclipse.jgit.revwalk.RevCommit;
import lombok.Data;

@Data
public class CommitLog {

  private final RevCommit commit;
  private final Path fileName;

  public String getCommitId() {
    return commit.getName();
  }

  public String getCommitMessage() {
    return commit.getFullMessage();
  }
}

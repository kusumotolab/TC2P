package com.github.kusumotolab.tc2p.core.entities;

import org.eclipse.jgit.revwalk.RevCommit;
import lombok.Data;

@Data
public class FileRevision {
  private final FileRef src;
  private final FileRef dst;

  @Data
  public static class FileRef {
    private final String name;
    private final RevCommit commit;

    public String getCommitId() {
      return commit.getName();
    }

    public String getCommitMessage() {
      return commit.getFullMessage();
    }
  }
}

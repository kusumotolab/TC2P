package com.github.kusumotolab.tc2p.core.entities;

import com.github.kusumotolab.tc2p.tools.git.CommitLog;

public class CommitLogPair {

  private final CommitLog srcCommitLog;
  private final CommitLog dstCommitLog;

  public CommitLogPair(final CommitLog srcCommitLog, final CommitLog dstCommitLog) {
    this.srcCommitLog = srcCommitLog;
    this.dstCommitLog = dstCommitLog;
  }

  public CommitLog getSrcCommitLog() {

    return srcCommitLog;
  }

  public CommitLog getDstCommitLog() {
    return dstCommitLog;
  }
}

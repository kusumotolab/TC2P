package com.github.kusumotolab.tc2p.core.entities;

import java.util.List;
import com.github.kusumotolab.tc2p.tools.git.CommitLog;
import com.google.common.collect.Lists;

public class FileHistory {

  private final List<CommitLog> commitLogs;

  public FileHistory(final List<CommitLog> commitLogs) {
    this.commitLogs = commitLogs;
  }

  public List<CommitLogPair> trace() {
    final List<CommitLogPair> results = Lists.newArrayList();
    for (int i = 0; i < commitLogs.size() - 1; i++) {
      final CommitLog srcCommitLog = commitLogs.get(i);
      final CommitLog dstCommitLog = commitLogs.get(i + 1);
      results.add(new CommitLogPair(srcCommitLog, dstCommitLog));
    }
    return results;
  }
}

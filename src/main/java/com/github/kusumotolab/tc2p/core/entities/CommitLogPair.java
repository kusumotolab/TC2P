package com.github.kusumotolab.tc2p.core.entities;

import com.github.kusumotolab.tc2p.tools.git.CommitLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CommitLogPair {

  @Getter
  private final CommitLog srcCommitLog;
  @Getter
  private final CommitLog dstCommitLog;

}

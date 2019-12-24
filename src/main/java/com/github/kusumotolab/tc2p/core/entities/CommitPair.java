package com.github.kusumotolab.tc2p.core.entities;

import org.eclipse.jgit.revwalk.RevCommit;
import lombok.Data;

@Data
public class CommitPair {

  private final RevCommit srcCommit;
  private final RevCommit dstCommit;

}

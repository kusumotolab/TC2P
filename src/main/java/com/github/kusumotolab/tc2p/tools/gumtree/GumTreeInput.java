package com.github.kusumotolab.tc2p.tools.gumtree;

import lombok.Data;

@Data
public class GumTreeInput {

  private final String srcPath;
  private final String dstPath;
  private final String srcContents;
  private final String dstContents;
}

package com.github.kusumotolab.tc2p.tools.gumtree;

import java.nio.file.Path;

public class GumTreeInput {

  private final String srcPath;
  private final String dstPath;
  private final String srcContents;
  private final String dstContents;

  public GumTreeInput(final Path srcPath, final Path dstPath, final String srcContents,
      final String dstContents) {
    this(srcPath.toString(), dstPath.toString(), srcContents, dstContents);
  }

  public GumTreeInput(final String srcPath, final String dstPath, final String srcContents,
      final String dstContents) {
    this.srcPath = srcPath;
    this.dstPath = dstPath;
    this.srcContents = srcContents;
    this.dstContents = dstContents;
  }

  public String getSrcPath() {
    return srcPath;
  }

  public String getDstPath() {
    return dstPath;
  }

  public String getSrcContents() {
    return srcContents;
  }

  public String getDstContents() {
    return dstContents;
  }
}

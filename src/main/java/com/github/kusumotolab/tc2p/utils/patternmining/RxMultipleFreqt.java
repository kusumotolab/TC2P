package com.github.kusumotolab.tc2p.utils.patternmining;

import java.util.Set;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.google.common.collect.Sets;

public class RxMultipleFreqt extends RxFreqt {

  @Override
  protected boolean filterPattern(final TreePattern<ASTLabel> pattern, final int borderline) {
    return pattern.countPatten() >= borderline && isMultipleProjectsPattern(pattern);
  }

  private boolean isMultipleProjectsPattern(final TreePattern<ASTLabel> pattern) {
    final Set<String> projectBaseUrlSet = Sets.newHashSet();
    for (final String treeId : pattern.getTreeIds()) {
      final String baseURL = treeId.split("/compare")[0];
      projectBaseUrlSet.add(baseURL);
    }
    return projectBaseUrlSet.size() >= 2;
  }
}

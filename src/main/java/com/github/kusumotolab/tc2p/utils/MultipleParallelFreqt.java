package com.github.kusumotolab.tc2p.utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.google.common.collect.Multimap;

public class MultipleParallelFreqt extends ParallelFreqt {

  @Override
  protected Set<TreePattern<ASTLabel>> extractF1(final Set<Node<ASTLabel>> trees, final int borderline,
      final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache) {
    return super.extractF1(trees, borderline, countPatternCache).stream()
        .filter(this::isMultipleProjectsPattern)
        .collect(Collectors.toSet());
  }

  @Override
  protected boolean filterPattern(final TreePattern<ASTLabel> pattern, final int borderline) {
    return super.filterPattern(pattern, borderline) && isMultipleProjectsPattern(pattern);
  }

  private boolean isMultipleProjectsPattern(final TreePattern<ASTLabel> pattern) {
    final Set<String> projectBaseUrlSet = pattern.getTreeIds().stream()
        .map(e -> e.split("/compare/")[0])
        .collect(Collectors.toSet());
    return projectBaseUrlSet.size() >= 2;
  }
}

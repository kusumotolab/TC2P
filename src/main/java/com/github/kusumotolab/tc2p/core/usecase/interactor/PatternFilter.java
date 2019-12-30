package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.usecase.interactor.PatternFilter.Input;
import com.google.common.collect.Sets;
import lombok.Data;

public class PatternFilter implements Interactor<Input, Set<TreePattern<ASTLabel>>> {

  @Override
  public Set<TreePattern<ASTLabel>> execute(final Input input) {
    final Set<TreePattern<ASTLabel>> filteredNoActionPatterns = filterNoActionPattern(input.getPatterns());
    return filterDuplicatedPattern(filteredNoActionPatterns);
  }

  private Set<TreePattern<ASTLabel>> filterNoActionPattern(final Set<TreePattern<ASTLabel>> patterns) {
    return patterns.stream()
        .filter(pattern -> {
          final Node<ASTLabel> rootNode = pattern.getRootNode();
          final List<Node<ASTLabel>> descents = rootNode.getDescents();
          return descents.stream()
              .map(Node::getLabel)
              .map(ASTLabel::getActions)
              .anyMatch(e -> !e.isEmpty());
        }).collect(Collectors.toSet());
  }

  private Set<TreePattern<ASTLabel>> filterDuplicatedPattern(final Set<TreePattern<ASTLabel>> patterns) {
    final Set<TreePattern<ASTLabel>> removedPatterns = Sets.newHashSet(patterns);
    for (final TreePattern<ASTLabel> pattern : patterns) {
      final Node<ASTLabel> patternRootNode = pattern.getRootNode();
      final List<TreePattern<ASTLabel>> removeCandidatePatterns = removedPatterns.parallelStream()
          .filter(e -> !e.equals(pattern))
          .filter(e -> patternRootNode.contains(e.getRootNode()))
          .collect(Collectors.toList());
      removedPatterns.removeAll(removeCandidatePatterns);
    }

    return removedPatterns;
  }

  @Data
  public static class Input {

    private final Set<TreePattern<ASTLabel>> patterns;
  }
}

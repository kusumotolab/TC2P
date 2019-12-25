package com.github.kusumotolab.tc2p.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Freqt;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParallelFreqt extends Freqt<ASTLabel> {

  private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime()
      .availableProcessors());

  @Override
  public Set<TreePattern<ASTLabel>> mining(final Set<Node<ASTLabel>> trees, final double minimumSupport) {
    log.debug("Start mining");

    final Set<TreePattern<ASTLabel>> results = Sets.newHashSet();
    final int borderline = extractBorderline(trees, minimumSupport);
    log.debug("Finish calculating Borderline");

    final Set<TreePattern<ASTLabel>> f1 = extractF1(trees, borderline);
    results.addAll(f1);
    log.debug("Finish mining f1 (" + f1.size() + ")");

    final Set<TreePattern<ASTLabel>> f2 = extractF2(trees, f1, borderline);
    results.addAll(f2);
    log.debug("Finish mining f2 (" + f2.size() + ")");

    final Multimap<List<ASTLabel>, ASTLabel> rightMostCacheMap = HashMultimap.create();
    updateRightMostCacheMap(f2, rightMostCacheMap);

    Set<TreePattern<ASTLabel>> fk = f2;
    int k = 2;
    while (!fk.isEmpty()) {
      final Set<TreePattern<ASTLabel>> fkPlus1 = extractFkPlus1(trees, fk, rightMostCacheMap, borderline);
      results.addAll(fkPlus1);
      fk = fkPlus1;
      k += 1;
      log.debug("Finish mining f" + k + "(" + fk.size() + ")");
      updateRightMostCacheMap(fk, rightMostCacheMap);
    }
    log.debug("Finish mining");
    threadPool.shutdown();
    return results;
  }

  private int extractBorderline(final Set<Node<ASTLabel>> trees, final double minimumSupport) {
    final int sum = (int) trees.parallelStream()
        .map(Node::getDescents)
        .mapToLong(Collection::size)
        .sum();
    return (int) (((double) sum) * minimumSupport);
  }

  private Set<TreePattern<ASTLabel>> extractF1(final Set<Node<ASTLabel>> trees, final int borderline) {
    final Map<ASTLabel, Integer> map = trees.parallelStream()
        .map(Node::getDescents)
        .flatMap(Collection::stream)
        .map(Node::getLabel)
        .collect(Collectors.toMap(e -> e, e -> 1, Integer::sum));

    return map.entrySet()
        .parallelStream()
        .filter(e -> {
          final Integer count = e.getValue();
          return count >= borderline;
        })
        .map(e -> new TreePattern<>(Node.createRootNode(e.getKey()), e.getValue()))
        .collect(Collectors.toSet());
  }

  private Set<TreePattern<ASTLabel>> extractF2(final Set<Node<ASTLabel>> trees, final Set<TreePattern<ASTLabel>> f1,
      final int borderline) {
    final List<Future<Set<Node<ASTLabel>>>> futures = Lists.newArrayList();

    for (final TreePattern<ASTLabel> element1 : f1) {
      final ASTLabel label1 = element1.getRootNode()
          .getLabel();
      futures.add(threadPool.submit(() -> {
        final Set<Node<ASTLabel>> candidates = Sets.newHashSet();
        for (final TreePattern<ASTLabel> element2 : f1) {
          final Node<ASTLabel> root = Node.createRootNode(label1);
          root.createChildNode(element2.getRootNode()
              .getLabel());
          candidates.add(root);
        }
        return candidates;
      }));
    }

    final Set<Node<ASTLabel>> candidates = futures.parallelStream()
        .map(future -> Try.force(future::get))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    return filterOverBorderLineAndMap(trees, borderline, candidates);
  }

  private void updateRightMostCacheMap(final Set<TreePattern<ASTLabel>> fk,
      final Multimap<List<ASTLabel>, ASTLabel> rightMostCacheMap) {
    fk.parallelStream()
        .forEach(pattern -> {
          final Node<ASTLabel> rootNode = pattern.getRootNode();
          final List<ASTLabel> rightMostBranch = rootNode.getRightMostBranch()
              .stream()
              .map(Node::getLabel)
              .collect(Collectors.toList());
          final ASTLabel last = rightMostBranch.remove(rightMostBranch.size() - 1);
          synchronized (this) {
            rightMostCacheMap.put(rightMostBranch, last);
          }
        });
    log.debug("Finish updating RMB");
  }

  private Set<TreePattern<ASTLabel>> extractFkPlus1(final Set<Node<ASTLabel>> trees, final Set<TreePattern<ASTLabel>> fk,
      final Multimap<List<ASTLabel>, ASTLabel> cache, final int borderline) {
    final List<Future<Set<Node<ASTLabel>>>> futures = Lists.newArrayList();

    for (final TreePattern<ASTLabel> treePattern : fk) {
      final Node<ASTLabel> rootNode = treePattern.getRootNode();
      final List<Node<ASTLabel>> rightMostBranch = rootNode.getRightMostBranch();
      for (int index = 0; index < rightMostBranch.size(); index++) {
        // Nodeのタイプとf2から挿入するラベルを選ぶ
        final int finalIndex = index;
        futures.add(threadPool.submit(() -> {
          final Set<Node<ASTLabel>> results = Sets.newHashSet();
          final List<ASTLabel> key = Lists.newArrayList(rightMostBranch.subList(0, finalIndex + 1)).stream().map(
              Node::getLabel).collect(Collectors.toList());
          if (rootNode.getDescents().size() == key.size()) {
            key.remove(0);
          }

          final Set<ASTLabel> candidateLabels = Sets.newHashSet(cache.get(key));
          for (final ASTLabel candidate : candidateLabels) {
            final Node<ASTLabel> copiedRootNode = rootNode.deepCopy();
            copiedRootNode.getRightMostBranch()
                .get(finalIndex)
                .createChildNode(candidate);
            results.add(copiedRootNode);
          }
          return results;
        }));
      }
    }

    final Set<Node<ASTLabel>> candidates = futures.parallelStream()
        .map(future -> Try.force(future::get))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    return filterOverBorderLineAndMap(trees, borderline, candidates);
  }

  private Set<TreePattern<ASTLabel>> filterOverBorderLineAndMap(final Set<Node<ASTLabel>> rootTrees, final int borderline,
      final Set<Node<ASTLabel>> candidates) {
    final List<Future<TreePattern<ASTLabel>>> futures = candidates.parallelStream()
        .filter(node -> !isUnnecessaryPattern(node))
        .map(candidate -> threadPool.submit(() -> {
          final int count = countPattern(rootTrees, candidate);
          return new TreePattern<>(candidate, count);
        })).collect(Collectors.toList());

    return futures.stream()
        .map(future -> Try.force(future::get))
        .filter(pattern -> filterPattern(pattern, borderline))
        .collect(Collectors.toSet());
  }


  private boolean filterPattern(final TreePattern<ASTLabel> pattern, final int borderline) {
    return pattern.countPatten() >= borderline;
  }

  private boolean isUnnecessaryPattern(final Node<ASTLabel> patternRootNode) {
    final List<Node<ASTLabel>> rightMostBranch = patternRootNode.getRightMostBranch();

    // 0: 拡張したノードを除いたRMBに、移動がないかの確認
    for (int i = 0; i < rightMostBranch.size() - 1; i++) {
      final Node<ASTLabel> node = rightMostBranch.get(i);
      final List<ActionEnum> actions = node.getLabel().getActions();
      if (actions.contains(ActionEnum.DST_MOVE) || actions.contains(ActionEnum.SRC_MOV)) {
        return false;
      }
    }

    // 1: 拡張したノードの左のノードを探す
    final Node<ASTLabel> extendNode = rightMostBranch.get(rightMostBranch.size() - 1);
    final Node<ASTLabel> parentNode = extendNode.getParent();
    final List<Node<ASTLabel>> siblingNodes = parentNode.getChildren();
    if (siblingNodes.size() <= 1) {
      return false;
    }
    final Node<ASTLabel> leftNode = siblingNodes.get(siblingNodes.size() - 2);

    // 左のノードの子ノードを確認する
    return leftNode.getDescents().stream()
        .allMatch(descent -> descent.getLabel().getActions().isEmpty());
  }

  private int countPattern(final Set<Node<ASTLabel>> rootTrees, final Node<ASTLabel> subtree) {
    return rootTrees.stream()
        .map(node -> node.countPatterns(subtree))
        .reduce(0, Integer::sum);
  }
}

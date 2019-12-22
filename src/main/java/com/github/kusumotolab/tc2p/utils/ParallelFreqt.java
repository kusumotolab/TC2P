package com.github.kusumotolab.tc2p.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Freqt;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParallelFreqt<T> extends Freqt<T> {

  private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime()
      .availableProcessors());

  @Override
  public Set<TreePattern<T>> mining(final Set<Node<T>> trees, final double minimumSupport) {
    log.debug("Start mining");

    final Set<TreePattern<T>> results = Sets.newHashSet();
    final int borderline = extractBorderline(trees, minimumSupport);
    log.debug("Finish calculating Borderline");

    final Set<TreePattern<T>> f1 = extractF1(trees, borderline);
    results.addAll(f1);
    log.debug("Finish mining f1 (" + f1.size() + ")");

    final Set<TreePattern<T>> f2 = extractF2(trees, f1, borderline);
    results.addAll(f2);
    log.debug("Finish mining f2 (" + f2.size() + ")");

    final Multimap<List<T>, T> rightMostCacheMap = HashMultimap.create();
    updateRightMostCacheMap(f2, rightMostCacheMap);

    Set<TreePattern<T>> fk = f2;
    int k = 2;
    while (!fk.isEmpty()) {
      final Set<TreePattern<T>> fkPlus1 = extractFkPlus1(trees, fk, rightMostCacheMap, borderline);
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

  private int extractBorderline(final Set<Node<T>> trees, final double minimumSupport) {
    final int sum = (int) trees.parallelStream()
        .map(Node::getDescents)
        .mapToLong(Collection::size)
        .sum();
    return (int) (((double) sum) * minimumSupport);
  }

  private Set<TreePattern<T>> extractF1(final Set<Node<T>> trees, final int borderline) {
    final Map<T, Integer> map = trees.parallelStream()
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

  private Set<TreePattern<T>> extractF2(final Set<Node<T>> trees, final Set<TreePattern<T>> f1,
      final int borderline) {
    final List<Future<Set<Node<T>>>> futures = Lists.newArrayList();

    for (final TreePattern<T> element1 : f1) {
      final T label1 = element1.getRootNode()
          .getLabel();
      futures.add(threadPool.submit(() -> {
        final Set<Node<T>> candidates = Sets.newHashSet();
        for (final TreePattern<T> element2 : f1) {
          final Node<T> root = Node.createRootNode(label1);
          root.createChildNode(element2.getRootNode()
              .getLabel());
          candidates.add(root);
        }
        return candidates;
      }));
    }

    final Set<Node<T>> candidates = futures.parallelStream()
        .map(UncheckedFuture::new)
        .map(UncheckedFuture::get)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    return filterOverBorderLineAndMap(trees, borderline, candidates);
  }


  private void updateRightMostCacheMap(final Set<TreePattern<T>> fk,
      final Multimap<List<T>, T> rightMostCacheMap) {
    fk.parallelStream()
        .forEach(pattern -> {
          final Node<T> rootNode = pattern.getRootNode();
          final List<T> rightMostBranch = rootNode.getRightMostBranch()
              .stream()
              .map(Node::getLabel)
              .collect(Collectors.toList());
          final T last = rightMostBranch.remove(rightMostBranch.size() - 1);
          synchronized (this) {
            rightMostCacheMap.put(rightMostBranch, last);
          }
        });
    log.debug("Finish updating RMB");
  }

  private Set<TreePattern<T>> extractFkPlus1(final Set<Node<T>> trees, final Set<TreePattern<T>> fk,
      final Multimap<List<T>, T> cache, final int borderline) {
    final List<Future<Set<Node<T>>>> futures = Lists.newArrayList();

    for (final TreePattern<T> treePattern : fk) {
      final Node<T> rootNode = treePattern.getRootNode();
      final List<Node<T>> rightMostBranch = rootNode.getRightMostBranch();
      for (int index = 0; index < rightMostBranch.size(); index++) {
        // Nodeのタイプとf2から挿入するラベルを選ぶ
        final int finalIndex = index;
        futures.add(threadPool.submit(() -> {
          final Set<Node<T>> results = Sets.newHashSet();
          final List<T> key = Lists.newArrayList(rightMostBranch.subList(0, finalIndex + 1)).stream().map(
              Node::getLabel).collect(Collectors.toList());
          if (rootNode.getDescents().size() == key.size()) {
            key.remove(0);
          }

          final Set<T> candidateLabels = Sets.newHashSet(cache.get(key));
          for (final T candidate : candidateLabels) {
            final Node<T> copiedRootNode = rootNode.deepCopy();
            copiedRootNode.getRightMostBranch()
                .get(finalIndex)
                .createChildNode(candidate);
            results.add(copiedRootNode);
          }
          return results;
        }));
      }
    }

    final Set<Node<T>> candidates = futures.parallelStream()
        .map(UncheckedFuture::new)
        .map(UncheckedFuture::get)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    return filterOverBorderLineAndMap(trees, borderline, candidates);
  }

  private Set<TreePattern<T>> filterOverBorderLineAndMap(final Set<Node<T>> rootTrees,
      final int borderline, final Set<Node<T>> candidates) {
    return candidates.parallelStream()
        .map(candidate -> threadPool.submit(() -> {
          final int count = countPattern(rootTrees, candidate);
          return new TreePattern<>(candidate, count);
        }))
        .map(pattern -> Try.force(pattern::get))
        .filter(e -> e.countPatten() >= borderline)
        .collect(Collectors.toSet());
  }

  private int countPattern(final Set<Node<T>> rootTrees, final Node<T> subtree) {
    return rootTrees.parallelStream()
        .map(e -> e.countPatterns(subtree))
        .reduce(0, Integer::sum);
  }

  private static class UncheckedFuture<T> {

    private final Future<T> future;

    UncheckedFuture(final Future<T> future) {
      this.future = future;
    }

    T get() {
      try {
        return future.get();
      } catch (final InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      return null;
    }
  }
}

package com.github.kusumotolab.tc2p.utils.patternmining;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RxFreqt {

  private ExecutorService threadPool = Executors
      .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public Observable<TreePattern<ASTLabel>> mining(final Set<Node<ASTLabel>> trees, final double minimumSupport) {
    return Observable.create(emitter -> {

      log.debug("Start mining");
      final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache = HashMultimap.create();
      final int borderline = extractBorderline(trees, minimumSupport);
      log.debug("Finish calculating Borderline");

      final Set<TreePattern<ASTLabel>> f1 = extractF1(trees, borderline, countPatternCache, emitter);
      removeUnnecessaryRootTrees(trees, f1);
      log.debug("Finish mining f1 (" + f1.size() + ")");

      final Set<TreePattern<ASTLabel>> f2 = extractF2(f1, borderline, countPatternCache, emitter);
      f1.clear();
      removeUnnecessaryCache(1, countPatternCache);
      log.debug("Finish mining f2 (" + f2.size() + ")");

      final Multimap<List<ASTLabel>, ASTLabel> rightMostCacheMap = HashMultimap.create();
      updateRightMostCacheMap(f2, rightMostCacheMap);

      Set<TreePattern<ASTLabel>> fk = f2;
      int k = 2;
      while (!fk.isEmpty()) {
        final Set<TreePattern<ASTLabel>> fkPlus1 = extractFkPlus1(fk, rightMostCacheMap, borderline,
            countPatternCache, emitter);
        fk.clear();
        removeUnnecessaryCache(k, countPatternCache);
        fk = fkPlus1;
        k += 1;
        log.debug("Finish mining f" + k + " (" + fk.size() + ")");
        updateRightMostCacheMap(fk, rightMostCacheMap);
      }
      log.debug("Finish mining");
      emitter.onComplete();
    });
  }

  public Completable shutdown() {
    return Completable.create(emitter -> {
      threadPool.shutdown();
      Try.lambda(() -> threadPool.awaitTermination(10, TimeUnit.DAYS));
      emitter.onComplete();
    });
  }

  protected int extractBorderline(final Set<Node<ASTLabel>> trees, final double minimumSupport) {
    final int sum = (int) trees.stream()
        .map(Node::getDescents)
        .mapToLong(Collection::size)
        .sum();
    return (int) (((double) sum) * minimumSupport);
  }

  protected Set<TreePattern<ASTLabel>> extractF1(final Set<Node<ASTLabel>> trees, final int borderline,
      final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache, final ObservableEmitter<TreePattern<ASTLabel>> emitter) {

    final Multimap<ASTLabel, String> idMap = HashMultimap.create();
    final Map<ASTLabel, Integer> map = trees.parallelStream()
        .map(Node::getDescents)
        .flatMap(descents -> descents.stream()
            .peek(node -> {
              final Label<ASTLabel> label = new Label<>(0, node.getLabel());
              synchronized (countPatternCache) {
                countPatternCache.put(Lists.newArrayList(label), node);
              }
              synchronized (idMap) {
                idMap.put(node.getLabel(), node.getTreeId());
              }
            })
            .distinct()
            .map(Node::getLabel))
        .collect(Collectors.toMap(e -> e, e -> 1, Integer::sum));

    final Set<TreePattern<ASTLabel>> results = map.entrySet().stream()
        .filter(e -> {
          final Integer count = e.getValue();
          final boolean flag = count >= borderline;
          if (!flag) {
            final Label<ASTLabel> label = new Label<>(0, e.getKey());
            countPatternCache.removeAll(Lists.newArrayList(label));
          }
          return flag;
        })
        .parallel()
        .map(e -> {
          final ASTLabel label = e.getKey();
          final Integer count = e.getValue();
          final Set<String> ids = Sets.newHashSet(idMap.get(label));
          return new TreePattern<>(Node.createRootNode("", label), ids, count);
        })
        .filter(e -> this.filterPattern(e, borderline))
        .collect(Collectors.toSet());
    results.forEach(emitter::onNext);
    return results;
  }

  protected Set<TreePattern<ASTLabel>> extractF2(final Set<TreePattern<ASTLabel>> f1, final int borderline,
      final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache, final ObservableEmitter<TreePattern<ASTLabel>> emitter) {
    final List<Future<?>> futures = Lists.newArrayList();

    final Set<TreePattern<ASTLabel>> results = Sets.newConcurrentHashSet();
    for (final TreePattern<ASTLabel> element1 : f1) {
      final String treeId = element1.getRootNode().getTreeId();
      final ASTLabel label = element1.getRootNode().getLabel();

      // SimpleNameの下にノードは存在しない
      if (label.getType().equals("SimpleName")) {
        continue;
      }

      futures.add(threadPool.submit(() -> {
        for (final TreePattern<ASTLabel> element2 : f1) {
          final Node<ASTLabel> root = Node.createRootNode(treeId, label);
          root.createChildNode(element2.getRootNode().getLabel());
          final Optional<TreePattern<ASTLabel>> patternOptional = filterOverBorderLineAndMap(borderline, root, countPatternCache);
          patternOptional.ifPresent(pattern -> {
            emitter.onNext(pattern);
            results.add(pattern);
          });
        }
      }));
    }

    futures.forEach(future -> Try.lambda(() -> future.get(1, TimeUnit.DAYS)));

    return results;
  }

  protected void updateRightMostCacheMap(final Set<TreePattern<ASTLabel>> fk, final Multimap<List<ASTLabel>, ASTLabel> rightMostCacheMap) {
    fk.forEach(pattern -> {
      final Node<ASTLabel> rootNode = pattern.getRootNode();
      final List<ASTLabel> rightMostBranch = rootNode.getRightMostBranch()
          .stream()
          .map(Node::getLabel)
          .collect(Collectors.toList());
      if (rightMostBranch.size() != rootNode.getDescents().size()) {
        return;
      }
      final ASTLabel last = rightMostBranch.remove(rightMostBranch.size() - 1);
      rightMostCacheMap.put(rightMostBranch, last);
    });
  }

  protected Set<TreePattern<ASTLabel>> extractFkPlus1(final Set<TreePattern<ASTLabel>> fk, final Multimap<List<ASTLabel>, ASTLabel> cache,
      final int borderline, final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache,
      final ObservableEmitter<TreePattern<ASTLabel>> emitter) {
    final List<Future<?>> futures = Lists.newArrayList();
    final Set<TreePattern<ASTLabel>> results = Sets.newConcurrentHashSet();

    for (final TreePattern<ASTLabel> treePattern : fk) {
      final Node<ASTLabel> rootNode = treePattern.getRootNode();
      final List<Node<ASTLabel>> rightMostBranch = rootNode.getRightMostBranch();

      futures.add(threadPool.submit(() -> {
        for (int index = 0; index < rightMostBranch.size(); index++) {
          final Node<ASTLabel> extendNode = rightMostBranch.get(index);
          if (extendNode.getLabel().getType().equals("SimpleName")) {
            continue;
          }

          // Nodeのタイプとf2から挿入するラベルを選ぶ
          final List<ASTLabel> rightMostBranchKey = Lists
              .newArrayList(rightMostBranch.subList(0, index + 1)).stream()
              .map(Node::getLabel)
              .collect(Collectors.toList());
          if (rootNode.getDescents().size() == rightMostBranchKey.size()) {
            rightMostBranchKey.remove(0);
          }

          final Set<ASTLabel> candidateLabels = Sets.newHashSet(cache.get(rightMostBranchKey));
          for (final ASTLabel candidate : candidateLabels) {
            final Node<ASTLabel> copiedRootNode = rootNode.deepCopy();
            copiedRootNode.getRightMostBranch()
                .get(index)
                .createChildNode(candidate);
            final Optional<TreePattern<ASTLabel>> pattern = filterOverBorderLineAndMap(borderline, copiedRootNode, countPatternCache);
            pattern.ifPresent(p -> {
              emitter.onNext(p);
              results.add(p);
            });
          }
        }
      }));
    }

    futures.forEach(future -> Try.lambda(() -> future.get(1, TimeUnit.DAYS)));
    return results;
  }


  protected Optional<TreePattern<ASTLabel>> filterOverBorderLineAndMap(final int borderline, final Node<ASTLabel> candidate,
      final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache) {
    if (isUnnecessaryPattern(candidate)) {
      return Optional.empty();
    }

    final CountResult countResult = countPattern(candidate, countPatternCache);
    final TreePattern<ASTLabel> treePattern = new TreePattern<>(candidate, countResult.getIds(), countResult.getCount());
    if (!filterPattern(treePattern, borderline)) {
      return Optional.empty();
    }
    return Optional.of(treePattern);
  }

  protected boolean filterPattern(final TreePattern<ASTLabel> pattern, final int borderline) {
    return pattern.countPatten() >= borderline;
  }

  protected boolean isUnnecessaryPattern(final Node<ASTLabel> patternRootNode) {
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

  protected CountResult countPattern(final Node<ASTLabel> subtree,
      final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache) {
    final List<Label<ASTLabel>> subtreeLabels = Lists.newArrayList(subtree.getLabels());
    subtreeLabels.remove(subtreeLabels.size() - 1);

    final Set<String> rootIds = Sets.newHashSet();
    return countPatternCache.get(subtreeLabels).stream()
        .map(node -> {
          final boolean contains = node.contains(subtree);
          if (contains) {
            final List<Label<ASTLabel>> labels = subtree.getLabels();
            synchronized (countPatternCache) {
              countPatternCache.put(labels, node);
            }
          }
          return new CountResult(contains ? 1 : 0, Sets.newHashSet(node.getTreeId()));
        })
        .filter(e -> e.count > 0)
        .filter(countResult -> {
          if (rootIds.containsAll(countResult.ids)) {
            return false;
          }
          rootIds.addAll(countResult.ids);
          return true;
        })
        .reduce((e1, e2) -> {
          final Set<String> set = Sets.newHashSet(e1.ids);
          set.addAll(e2.ids);
          return new CountResult(e1.count + e2.count, set);
        })
        .orElse(new CountResult(0, Collections.emptySet()));
  }

  protected void removeUnnecessaryCache(final int removeSize, final Multimap<List<Label<ASTLabel>>, Node<ASTLabel>> countPatternCache) {
    final List<List<Label<ASTLabel>>> removedKeys = countPatternCache.keys()
        .parallelStream()
        .filter(labels -> labels.size() == removeSize)
        .collect(Collectors.toList());
    removedKeys.forEach(countPatternCache::removeAll);
  }

  protected void removeUnnecessaryRootTrees(final Set<Node<ASTLabel>> roots, final Set<TreePattern<ASTLabel>> f1) {
    final Set<Node<ASTLabel>> removedSet = roots.parallelStream()
        .filter(root -> {
          for (final TreePattern<ASTLabel> pattern : f1) {
            if (root.contains(pattern.getRootNode())) {
              return false;
            }
          }
          return true;
        }).collect(Collectors.toSet());
    roots.removeAll(removedSet);
  }

  @Data
  protected static class CountResult {

    private final int count;
    private final Set<String> ids;
  }
}
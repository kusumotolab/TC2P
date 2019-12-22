package com.github.kusumotolab.tc2p.core.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.sdl4j.util.Measure;
import com.github.kusumotolab.sdl4j.util.Measure.MeasuredResult;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.github.kusumotolab.tc2p.core.presenter.IMiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.EditScriptFetcher;
import com.github.kusumotolab.tc2p.core.usecase.interactor.PatternFilter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.ParallelFreqt;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MiningEditPatternUseCase<V extends View, P extends IMiningEditPatternPresenter<V>> extends
    IMiningPatternUseCase<V, P> {

  public MiningEditPatternUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Set<Node<ASTLabel>> trees = Sets.newHashSet();

    final EditScriptFetcher.Input editScriptFetcherInput = new EditScriptFetcher.Input(input.getProjectName());
    final List<EditScript> editScripts = new EditScriptFetcher().execute(editScriptFetcherInput);
    presenter.endFetchEditScript(editScripts);

    editScripts.stream()
        .filter(e -> !e.getTreeNodeIds().isEmpty())
        .map(this::convertToNode)
        .forEach(trees::add);
    presenter.endConstructingTrees(trees);

    final double minimumSupport = calculateMinimumSupport(trees, input.getFrequency());
    final ParallelFreqt<ASTLabel> freqt = new ParallelFreqt<>();
    final MeasuredResult<Set<TreePattern<ASTLabel>>> measuredResult = Measure.time(() -> freqt.mining(trees, minimumSupport));
    presenter.time("Freqt", measuredResult.getDuration());

    final Set<TreePattern<ASTLabel>> patterns = measuredResult.getValue();
    final MeasuredResult<Set<TreePattern<ASTLabel>>> patternFilterResult = Measure
        .time(() -> new PatternFilter().execute(new PatternFilter.Input(patterns)));
    presenter.time("Filtering", patternFilterResult.getDuration());

    patternFilterResult.getValue().stream()
        .sorted(Comparator.comparingInt(e -> e.getRootNode().getDescents().size()))
        .forEach(presenter::pattern);
  }

  private Node<ASTLabel> convertToNode(final EditScript editScript) {
    final TreeNode compactedRootNode = compaction(editScript.getTreeNodes().get(0));
    final Node<ASTLabel> rootNode = Node.createRootNode(new ASTLabel(compactedRootNode));
    final Map<Integer, Node<ASTLabel>> map = Maps.newHashMap();
    map.put(rootNode.getLabel().getId(), rootNode);

    final List<TreeNode> descents = compactedRootNode.getDescents();
    for (int i = 1; i < descents.size(); i++) {
      final TreeNode node = descents.get(i);
      final ASTLabel label = new ASTLabel(node);
      final Node<ASTLabel> parentNode = map.get(label.getParentId());

      final Node<ASTLabel> childNode = parentNode.createChildNode(label);
      map.put(childNode.getLabel().getId(), childNode);
    }
    return rootNode;
  }

  private TreeNode compaction(final TreeNode treeNode) {
    final TreeNode rootNode = treeNode.compactAndGetNewRootNode();
    rootNode.fixId();
    return rootNode;
  }

  private double calculateMinimumSupport(final Set<Node<ASTLabel>> trees, int frequency) {
    final Integer totalSize = trees.stream()
        .map(e -> e.getDescents().size())
        .reduce(Integer::sum)
        .orElse(0);
    return ((double) frequency) / totalSize.doubleValue();
  }
}

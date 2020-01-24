package com.github.kusumotolab.tc2p.core.usecase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.PatternPosition;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.github.kusumotolab.tc2p.core.presenter.IMiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.EditScriptFetcher;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.utils.patternmining.RxFreqt;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class RxMiningEditPatternUseCase<V extends View, P extends IMiningEditPatternPresenter<V>> extends IMiningPatternUseCase<V, P> {

  public RxMiningEditPatternUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final Set<Node<ASTLabel>> trees = Sets.newHashSet();

    presenter.startFetchEditScript();
    final EditScriptFetcher.Input editScriptFetcherInput = new EditScriptFetcher.Input(input.getProjectName());
    final List<EditScript> editScripts = new EditScriptFetcher().execute(editScriptFetcherInput);
    presenter.endFetchEditScript(editScripts);

    editScripts.stream()
        .filter(e -> !e.getTreeNodeIds().isEmpty())
        .map(this::convertToNode)
        .forEach(trees::add);
    presenter.endConstructingTrees(trees);
    editScripts.clear();

    final double minimumSupport = calculateMinimumSupport(trees, input.getFrequency());
    final RxFreqt freqt = new RxFreqt();
    final AtomicInteger treeNo = new AtomicInteger(0);
    final Observable<MiningResult> miningResults = freqt.mining(trees, minimumSupport)
        .subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.io())
        .map(pattern -> {
          final int no = treeNo.addAndGet(1);
          return new MiningResult(no, input.getProjectName(), extractPatternPosition(pattern.getTreeIds()), pattern);
        });

    /*
    final SQLite sqLite = new SQLite("ignore/tc2p-results-DB/" + input.getProjectName() + "__" + input.getFrequency() + ".sqlite");
    sqLite.connect()
        .andThen(sqLite.createTable(MiningResult.class))
        .andThen(sqLite.insert(miningResults, 100000))
        .andThen(sqLite.close())
        .andThen(freqt.shutdown())
        .blockingAwait();
     */
    Completable.fromObservable(miningResults)
        .andThen(freqt.shutdown())
        .blockingAwait();
    presenter.show("# of Patterns: " + treeNo.get());
    presenter.time("Total Time", stopwatch.elapsed());

  }

  private boolean hasAction(final TreePattern<ASTLabel> pattern) {
    return !pattern.getRootNode().getLabels().stream()
        .map(Label::getLabel)
        .map(ASTLabel::getActions)
        .allMatch(List::isEmpty);
  }

  private Node<ASTLabel> convertToNode(final EditScript editScript) {
    final TreeNode compactedRootNode = compaction(editScript.getTreeNodes().get(0));
    final String nodeId = extractIdFromTree(editScript);
    final Node<ASTLabel> rootNode = Node.createRootNode(nodeId, new ASTLabel(compactedRootNode));
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

  private String extractIdFromTree(final EditScript editScript) {
    return constructGitHubURLFromMJava(editScript);
  }

  private String constructGitHubURLFromMJava(final EditScript editScript) {
    final String[] split = editScript.getProjectName().split("__");
    final String userName = split[0];
    final String repositoryName = split[1];
    final String repositoryBaseURL = "https://github.com/" + userName + "/" + repositoryName;
    return repositoryBaseURL + "/compare/" + extractCommitIdFromFinerGitCommitMessage(editScript.getSrcCommitMessage()) + "..."
        + extractCommitIdFromFinerGitCommitMessage(editScript.getDstCommitMessage())
        + "    // " + editScript.getSrcName() + " <=> " + editScript.getDstName();
  }

  private PatternPosition extractPatternPosition(final String treeId) {
    final String url = treeId.split(" ")[0];
    final String mjavadiff = treeId.split("//")[1];
    return new PatternPosition(url, mjavadiff);
  }

  private List<PatternPosition> extractPatternPosition(final Set<String> treeIds) {
    return treeIds.stream()
        .map(this::extractPatternPosition)
        .collect(Collectors.toList());
  }

  private String extractCommitIdFromFinerGitCommitMessage(final String commitMessage) {
    return commitMessage.substring(commitMessage.indexOf(':') + 1, commitMessage.indexOf('>'));
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

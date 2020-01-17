package com.github.kusumotolab.tc2p.core.usecase;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.util.Measure;
import com.github.kusumotolab.sdl4j.util.Measure.MeasuredResult;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.presenter.IMiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.EditScriptFetcher;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ITNode;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ItemAndOccurrence;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.Occurrence;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ParallelItemBag;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.Transaction;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.TransactionID;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BaseUseCase<V extends View, P extends IMiningEditPatternPresenter<V>> extends IMiningPatternUseCase<V, P> {

  public BaseUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Stopwatch stopwatch = Stopwatch.createStarted();

    presenter.startFetchEditScript();
    final EditScriptFetcher.Input editScriptFetcherInput = new EditScriptFetcher.Input(input.getProjectName());
    final List<EditScript> editScripts = new EditScriptFetcher().execute(editScriptFetcherInput);
    presenter.endFetchEditScript(editScripts);

    final Set<Transaction<ASTLabel>> transactions = editScripts.stream()
        .filter(e -> !e.getTreeNodeIds().isEmpty())
        .map(this::convertToTransaction)
        .collect(Collectors.toSet());
//    presenter.endConstructingTrees(trees);
    editScripts.clear();

    final ParallelItemBag<ASTLabel> itemBag = new ParallelItemBag<>();
    final MeasuredResult<ITNode<ASTLabel>> measuredResult = Measure.time(() -> itemBag.mining(transactions, input.getFrequency()));
    itemBag.shutdown();
    presenter.time("Freqt", measuredResult.getDuration());

    final ITNode<ASTLabel> rootNode = measuredResult.getValue();
    System.out.println(rootNode.size());
//    final MeasuredResult<Set<TreePattern<ASTLabel>>> patternFilterResult = Measure
//        .time(() -> new PatternFilter().execute(new PatternFilter.Input(patterns)));
//    presenter.time("Filtering", patternFilterResult.getDuration());
//
//    patternFilterResult.getValue().stream()
//        .sorted(Comparator.comparingInt(e -> e.getRootNode().getDescents().size()))
//        .forEach(presenter::pattern);

//    presenter.endMiningPatterns(patternFilterResult.getValue());
    presenter.time("Total Time", stopwatch.elapsed());
  }

  private Transaction<ASTLabel> convertToTransaction(final EditScript editScript) {
    final String transactionId = extractIdFromTree(editScript);
    final List<ItemAndOccurrence<ASTLabel>> itemAndOccurrenceList = editScript.getTreeNodes().stream()
        .filter(e -> !e.getActions().isEmpty())
        .map(node -> {
          final Set<ItemAndOccurrence<ASTLabel>> items = Sets.newHashSet();
          final String nodeId = transactionId + "-" + node.getId();
          for (final ActionEnum action : node.getActions()) {
            final int parentId = node.getParentNode() != null ? node.getParentNode().getId() : -1;
            final ASTLabel label = new ASTLabel(node.getId(), parentId, Lists.newArrayList(action), node.getValue(), node.getNewValue(),
                node.getType());
            label.setComparator(new ASTLabelComparator());
            final Occurrence occurrence = new Occurrence(nodeId + "-" + action.toString());
            final ItemAndOccurrence<ASTLabel> itemAndOccurrence = new ItemAndOccurrence<>(label, occurrence);
            items.add(itemAndOccurrence);
          }
          return items;
        }).flatMap(Collection::stream)
        .collect(Collectors.toList());

    return new Transaction<>(new TransactionID(transactionId), itemAndOccurrenceList);
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

  private String extractCommitIdFromFinerGitCommitMessage(final String commitMessage) {
    return commitMessage.substring(commitMessage.indexOf(':') + 1, commitMessage.indexOf('>'));
  }


  private static class ASTLabelComparator extends ASTLabel.Comparator {

    @Override
    public boolean isEqual(final ASTLabel l1, final ASTLabel l2) {
      return l1.getActions().equals(l2.getActions())
          && l1.getType().equals(l2.getType());
    }

    @Override
    public int hash(final ASTLabel label) {
      return Objects.hash(label.getActions(), label.getType());
    }
  }
}

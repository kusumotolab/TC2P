package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.util.Measure;
import com.github.kusumotolab.sdl4j.util.Measure.MeasuredResult;
import com.github.kusumotolab.tc2p.core.entities.BaseLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.BaseResult;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.presenter.IMiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.EditScriptFetcher;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.utils.Colors;
import com.github.kusumotolab.tc2p.utils.FileUtil;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ITNode;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ItemAndOccurrence;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.Occurrence;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ParallelItemBag;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.Transaction;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.TransactionID;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    final Set<Transaction<BaseLabel>> transactions = editScripts.stream()
        .filter(e -> !e.getTreeNodeIds().isEmpty())
        .map(this::convertToTransaction)
        .collect(Collectors.toSet());
//    presenter.endConstructingTrees(trees);
    editScripts.clear();

    final ParallelItemBag<BaseLabel> itemBag = new ParallelItemBag<>();
    final MeasuredResult<ITNode<BaseLabel>> measuredResult = Measure.time(() -> itemBag.mining(transactions, input.getFrequency()));
    itemBag.shutdown();
    presenter.time("Freqt", measuredResult.getDuration());

    final ITNode<BaseLabel> rootNode = measuredResult.getValue();
    System.out.println(rootNode.size());

//    final List<ITNode<BaseLabel>> nodes = extractLeaf(rootNode, Lists.newArrayList()).stream()
//        .sorted(Comparator.comparingInt(node -> node.getItemSet().size()))
//        .collect(Collectors.toList());

    final List<ITNode<BaseLabel>> nodes = extractNode(rootNode, Lists.newArrayList()).stream()
        .sorted(Comparator.comparingInt(node -> node.getItemSet().size()))
        .collect(Collectors.toList());
    for (final ITNode<BaseLabel> node : nodes) {
      log.info("Frequency = " + node.maximumFrequency());
      for (final BaseLabel label : node.getItemSet()) {
        log.info("{action = " + label.getAction().toStringWithColor() + ", type =\"" + Colors.purple(label.getType()) + "\"}");
      }
      System.out.println();
    }

    final List<BaseResult> baseResults = nodes.stream()
        .map(node -> new BaseResult(input.getProjectName(), node))
        .collect(Collectors.toList());
    FileUtil.createDirectoryIfNeed(Paths.get("./ignore/base_results"));
    final SQLite sqLite = new SQLite("./ignore/base_results/" + input.getProjectName() + ".sqlite");

    sqLite.connect()
        .andThen(sqLite.createTable(BaseResult.class))
        .andThen(sqLite.insert(Observable.fromIterable(baseResults)))
        .andThen(sqLite.close())
        .blockingAwait();
//    final MeasuredResult<Set<TreePattern<BaseLabel>>> patternFilterResult = Measure
//        .time(() -> new PatternFilter().execute(new PatternFilter.Input(patterns)));
//    presenter.time("Filtering", patternFilterResult.getDuration());
//
//    patternFilterResult.getValue().stream()
//        .sorted(Comparator.comparingInt(e -> e.getRootNode().getDescents().size()))
//        .forEach(presenter::pattern);

//    presenter.endMiningPatterns(patternFilterResult.getValue());
    presenter.time("Total Time", stopwatch.elapsed());
  }


  private List<ITNode<BaseLabel>> extractLeaf(final ITNode<BaseLabel> root, final List<ITNode<BaseLabel>> results) {
    if (root.getChildren().isEmpty()) {
      results.add(root);
      return results;
    }
    for (final ITNode<BaseLabel> child : root.getChildren()) {
      extractLeaf(child, results);
    }
    return results;
  }

  private List<ITNode<BaseLabel>> extractNode(final ITNode<BaseLabel> root, final List<ITNode<BaseLabel>> results) {
    results.add(root);
    for (final ITNode<BaseLabel> child : root.getChildren()) {
      extractLeaf(child, results);
    }
    return results;
  }

  private Transaction<BaseLabel> convertToTransaction(final EditScript editScript) {
    final String transactionId = extractIdFromTree(editScript);
    final List<ItemAndOccurrence<BaseLabel>> itemAndOccurrenceList = editScript.getTreeNodes().stream()
        .filter(e -> !e.getActions().isEmpty())
        .map(node -> {
          final Set<ItemAndOccurrence<BaseLabel>> items = Sets.newHashSet();
          final String nodeId = transactionId + "-" + node.getId();
          for (final ActionEnum action : node.getActions()) {
            final BaseLabel label = new BaseLabel(node.getId(), action, node.getType());
            final Occurrence occurrence = new Occurrence(nodeId + "-" + action.toString());
            final ItemAndOccurrence<BaseLabel> itemAndOccurrence = new ItemAndOccurrence<>(label, occurrence);
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
}

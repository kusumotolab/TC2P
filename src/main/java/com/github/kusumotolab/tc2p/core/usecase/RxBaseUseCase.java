package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.BaseLabel;
import com.github.kusumotolab.tc2p.core.entities.BaseResult;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.github.kusumotolab.tc2p.core.presenter.IMiningEditPatternPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.EditScriptFetcher;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.utils.Colors;
import com.github.kusumotolab.tc2p.utils.FileUtil;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ItemAndOccurrence;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.Occurrence;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.RxlItemBag;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.Transaction;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.TransactionID;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RxBaseUseCase<V extends View, P extends IMiningEditPatternPresenter<V>> extends IMiningPatternUseCase<V, P> {

  public RxBaseUseCase(final P presenter) {
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
    editScripts.clear();
    final AtomicInteger atomicInteger = new AtomicInteger(0);
    final RxlItemBag<BaseLabel> itemBag = new RxlItemBag<>();
    final Observable<BaseResult> observable = itemBag.mining(transactions, input.getFrequency(), 300)
        .subscribeOn(Schedulers.computation())
        .observeOn(Schedulers.io())
        .doOnNext(node -> {
          log.info("No: " + atomicInteger.addAndGet(1));
          log.info("Frequency = " + node.maximumFrequency());
          node.getItemSet().stream()
              .map(this::toString)
              .forEach(log::info);
          System.out.println();
        }).map(e -> new BaseResult(input.getProjectName(), e));

    final Path baseResultsPath = Paths.get("./ignore/base-results");
    FileUtil.createDirectoryIfNeed(baseResultsPath);
    final SQLite sqLite = new SQLite(baseResultsPath.resolve(input.getProjectName() + ".sqlite").toString());

    sqLite.connect()
        .andThen(sqLite.createTable(BaseResult.class))
        .andThen(sqLite.insert(observable))
        .andThen(sqLite.close())
        .andThen(itemBag.shutdown())
        .blockingAwait();
    presenter.time("Total Time", stopwatch.elapsed());
  }

  private String toString(final BaseLabel label) {
    String text = "{action = " + label.getAction().toStringWithColor() + ", type = " + Colors.purple("\"" + label.getType() + "\"");
    return text + "}";
  }

  private enum State {
    IN_SRC_MOVE, IN_DST_MOVE, NONE
  }

  private void removeMove(final TreeNode treeNode, final State state) {
    if (treeNode.getActions().contains(ActionEnum.SRC_MOV)) {
      treeNode.getActions().clear();
      treeNode.getActions().add(ActionEnum.DEL);
      for (final TreeNode child : treeNode.getChildren()) {
        removeMove(child, State.IN_SRC_MOVE);
      }
    } else if (treeNode.getActions().contains(ActionEnum.DST_MOVE)) {
      treeNode.getActions().clear();
      treeNode.getActions().add(ActionEnum.INS);
      for (final TreeNode child : treeNode.getChildren()) {
        removeMove(child, State.IN_DST_MOVE);
      }
    } else if (state.equals(State.IN_SRC_MOVE)) {
      treeNode.getActions().clear();
      treeNode.getActions().add(ActionEnum.DEL);
      for (final TreeNode child : treeNode.getChildren()) {
        removeMove(child, State.IN_SRC_MOVE);
      }
    } else if (state.equals(State.IN_DST_MOVE)) {
      treeNode.getActions().clear();
      treeNode.getActions().add(ActionEnum.INS);
      for (final TreeNode child : treeNode.getChildren()) {
        removeMove(child, State.IN_DST_MOVE);
      }
    } else if (state.equals(State.NONE)) {
      for (final TreeNode child : treeNode.getChildren()) {
        removeMove(child, State.NONE);
      }
    }
  }

  private Transaction<BaseLabel> convertToTransaction(final EditScript editScript) {
    final String transactionId = extractIdFromTree(editScript);

    final TreeNode root = editScript.getTreeNodes().get(0);
    removeMove(root, State.NONE);

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
        .filter(e -> filter(e.getItem()))
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

  private static final Set<BaseLabel> filteredLabels = Sets.newHashSet(
      new BaseLabel(0, ActionEnum.DEL, "Block"),
      new BaseLabel(0, ActionEnum.INS, "Block"),
      new BaseLabel(0, ActionEnum.DEL, "VariableDeclarationStatement"),
      new BaseLabel(0, ActionEnum.INS, "VariableDeclarationStatement"),
      new BaseLabel(0, ActionEnum.DEL, "ExpressionStatement"),
      new BaseLabel(0, ActionEnum.INS, "ExpressionStatement"),
      new BaseLabel(0, ActionEnum.DEL, "MethodInvocation"),
      new BaseLabel(0, ActionEnum.INS, "MethodInvocation"),
      new BaseLabel(0, ActionEnum.UPD, "SimpleName"),
      new BaseLabel(0, ActionEnum.DEL, "SimpleName"),
      new BaseLabel(0, ActionEnum.INS, "SimpleName")
  );

  public static boolean filter(final BaseLabel label) {
    return !filteredLabels.contains(label);
  }
}

package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.github.gumtreediff.actions.model.Action;
import com.github.kusumotolab.tc2p.core.entities.CommitLogPair;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.github.kusumotolab.tc2p.core.entities.TreeNodeRawObject;
import com.github.kusumotolab.tc2p.core.usecase.interactor.SaveEditScriptInteractor.Input;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.git.CommitLog;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeInput;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeOutput;
import io.reactivex.Completable;
import io.reactivex.Observable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveEditScriptInteractor implements Interactor<Observable<Input>, Completable> {

  @Override
  public Completable execute(final Observable<Input> inputObservable) {
    final SQLite sqLite = new SQLite();

    return sqLite.connect()
        .andThen(sqLite.createTable(TreeNodeRawObject.class))
        .andThen(sqLite.createTable(EditScript.class))
        .andThen(inputObservable)
        .filter(input -> !input.getGumTreeOutput().getActions().isEmpty())
        .map(this::createEditScript)
        .flatMapCompletable(editScript -> {
          final Observable<TreeNodeRawObject> treeNodes = Observable
              .fromIterable(editScript.getTreeNodes())
              .map(TreeNode::asRaw);

          return sqLite.insert(treeNodes)
              .andThen(sqLite.insert(Observable.just(editScript)));
        }).andThen(sqLite.close());
  }

  private EditScript createEditScript(final Input input) {
    final CommitLogPair commitLogPair = input.getCommitLogPair();
    final CommitLog srcCommitLog = commitLogPair.getSrcCommitLog();
    final CommitLog dstCommitLog = commitLogPair.getDstCommitLog();

    final GumTreeOutput gumTreeOutput = input.getGumTreeOutput();
    final GumTreeInput gumTreeInput = gumTreeOutput.getInput();

    final EditScript editScript = new EditScript();

    editScript.setSrcName(gumTreeInput.getSrcPath());
    editScript.setSrcCommitID(srcCommitLog.getCommitId());
    editScript.setSrcCommitMessage(srcCommitLog.getCommitMessage());

    editScript.setDstName(gumTreeInput.getDstPath());
    editScript.setDstCommitID(dstCommitLog.getCommitId());
    editScript.setDstCommitMessage(dstCommitLog.getCommitMessage());

    editScript.setProjectName(input.getProjectName());

    createTreeNode(input).ifPresent(treeNode -> {
      final List<TreeNode> descents = treeNode.getDescents();
      final List<Integer> ids = descents.stream()
          .map(TreeNode::getId)
          .collect(Collectors.toList());
      editScript.setTreeNodes(descents);
      editScript.setTreeNodeIds(ids);
    });

    return editScript;
  }

  private Optional<TreeNode> createTreeNode(final Input input) {
    final CommitLogPair commitLogPair = input.getCommitLogPair();
    final CommitLog srcCommitLog = commitLogPair.getSrcCommitLog();
    final CommitLog dstCommitLog = commitLogPair.getDstCommitLog();

    final String srcPath = srcCommitLog.getFileName().toString();
    final String dstPath = dstCommitLog.getFileName().toString();

    final GumTreeOutput gumTreeOutput = input.getGumTreeOutput();
    final List<Action> actions = gumTreeOutput.getActions();

    final TreeNodeAdaptor.Input adapterInput = new TreeNodeAdaptor.Input(input.getProjectName(),
        srcCommitLog.getCommitId(), srcPath, dstCommitLog.getCommitId(), dstPath, gumTreeOutput.getMappingStore(),
        gumTreeOutput.getSrcTreeContext(), gumTreeOutput.getDstTreeContext(), actions);
    return new TreeNodeAdaptor().execute(adapterInput);
  }

  @Data
  public static class Input {

    private final String projectName;
    private final CommitLogPair commitLogPair;
    private final GumTreeOutput gumTreeOutput;
  }

}

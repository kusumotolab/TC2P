package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.github.kusumotolab.tc2p.core.entities.TreeNodeRawObject;
import com.github.kusumotolab.tc2p.core.usecase.interactor.EditScriptFetcher.Input;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;

public class EditScriptFetcher implements Interactor<Input, List<EditScript>> {

  @Override
  public List<EditScript> execute(final Input input) {
    final SQLite sqLite = new SQLite("./ignore/DB/" + input.getProjectName() + ".sqlite3");
    final Map<String, TreeNodeRawObject> treeNodeRawMap = createTreeNodeMap(input, sqLite);
    final Query<EditScript> query = createQuery();

    return sqLite.createTable(EditScript.class)
        .andThen(sqLite.fetch(query))
        .flatMap(e -> Observable.just(e).subscribeOn(Schedulers.computation()))
        .map(editScript -> {
          final String baseKey = createBaseKey(editScript);
          final Map<Integer, TreeNode> treeNodeMap = Maps.newHashMap();
          final List<TreeNode> treeNodes = Lists.newArrayList();
          for (final int treeNodeId : editScript.getTreeNodeIds()) {
            final TreeNodeRawObject rawObject = treeNodeRawMap.get(baseKey + treeNodeId);
            final TreeNode treeNode = rawObject.asTreeNode(treeNodeMap::get);
            treeNodeMap.put(treeNodeId, treeNode);
            treeNodes.add(treeNode);
          }
          editScript.setTreeNodes(treeNodes);
          return editScript;
        }).toList().doOnSuccess(e -> sqLite.close().blockingAwait()).blockingGet();
  }

  private Map<String, TreeNodeRawObject> createTreeNodeMap(final Input input, final SQLite sqLite) {
    return new TreeNodeFetcher(sqLite).execute(new TreeNodeFetcher.Input(input.getProjectName()))
        .stream()
        .collect(Collectors.toMap(this::createKey, e -> e));
  }

  private String createKey(final TreeNodeRawObject object) {
    return object.getProjectName()
        + object.getSrcCommitId()
        + object.getSrcFilePath()
        + object.getDstCommitId()
        + object.getDstFilePath()
        + object.getId();
  }

  private String createBaseKey(final EditScript editScript) {
    return editScript.getProjectName()
        + editScript.getSrcCommitID()
        + editScript.getSrcName()
        + editScript.getDstCommitID()
        + editScript.getDstName();
  }

  private Query<EditScript> createQuery() {
    return SQLiteQuery.select(EditScript.class)
        .from(EditScript.class)
        .build();
  }

  @Data
  public static class Input {

    private final String projectName;
  }
}

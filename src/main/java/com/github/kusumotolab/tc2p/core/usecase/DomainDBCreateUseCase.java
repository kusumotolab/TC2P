package com.github.kusumotolab.tc2p.core.usecase;

import java.util.List;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.github.kusumotolab.tc2p.core.entities.TreeNodeRawObject;
import com.github.kusumotolab.tc2p.core.usecase.interactor.EditScriptFetcher;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.google.common.collect.Lists;
import io.reactivex.Observable;

public class DomainDBCreateUseCase<V extends View,   P extends Presenter<V>> extends IDomainDBCreateUseCase<V, P> {

  public DomainDBCreateUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final List<EditScript> editScripts = Lists.newArrayList();
    for (final String project : input.getProjects()) {
      final String projectName = project.replace("/", "__");
      final EditScriptFetcher.Input editScriptFetcherInput = new EditScriptFetcher.Input(projectName);
      editScripts.addAll(new EditScriptFetcher().execute(editScriptFetcherInput));
    }
    final SQLite sqLite = new SQLite("ignore/DB/" + input.getDomain().replaceAll(" ", "_") + ".sqlite3");
    sqLite.connect()
        .andThen(sqLite.createTable(EditScript.class))
        .andThen(sqLite.createTable(TreeNodeRawObject.class))
        .andThen(sqLite.insert(Observable.fromIterable(editScripts)))
        .andThen(sqLite.insert(
            Observable.fromIterable(editScripts)
                .map(e -> e.getTreeNodes().stream().map(TreeNode::asRaw).collect(Collectors.toList()))
                .flatMap(Observable::fromIterable)
        )).blockingAwait();
  }
}

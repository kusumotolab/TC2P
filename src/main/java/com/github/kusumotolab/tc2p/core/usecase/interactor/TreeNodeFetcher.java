package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.util.List;
import com.github.kusumotolab.tc2p.core.entities.TreeNodeRawObject;
import com.github.kusumotolab.tc2p.core.usecase.interactor.TreeNodeFetcher.Input;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TreeNodeFetcher implements Interactor<Input, List<TreeNodeRawObject>> {

  private final SQLite sqLite;

  @Override
  public List<TreeNodeRawObject> execute(final Input input) {
    final Query<TreeNodeRawObject> query = createQuery();

    return sqLite.connect()
        .andThen(sqLite.createTable(TreeNodeRawObject.class))
        .andThen(sqLite.fetch(query))
        .toList()
        .blockingGet();
  }

  private Query<TreeNodeRawObject> createQuery() {

    return SQLiteQuery.select(TreeNodeRawObject.class)
        .from(TreeNodeRawObject.class)
        .build();
  }

  @Data
  public static class Input {
    private final String projectName;
  }

}

package com.github.kusumotolab.tc2p.core.usecase;

import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.core.entities.BaseLabel;
import com.github.kusumotolab.tc2p.core.entities.BaseResult;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import com.github.kusumotolab.tc2p.utils.FileUtil;
import com.google.common.collect.Sets;
import io.reactivex.Observable;

public class CompactBaseDBUseCase<V extends View, P extends Presenter<V>> extends ICompactBaseDBUseCase<V, P> {

  public CompactBaseDBUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    FileUtil.createDirectoryIfNeed(input.getOutputDBPath().getParent());

    final Set<Set<BaseLabel>> biggestActions = Sets.newConcurrentHashSet();

    final SQLite inputSQLite = new SQLite(input.getInputDBPath().toString());
    final SQLite outputSQLite = new SQLite(input.getOutputDBPath().toString());

    final Observable<BaseResult> baseResults = inputSQLite.connect()
        .andThen(outputSQLite.connect())
        .andThen(outputSQLite.createTable(BaseResult.class))
        .andThen(inputSQLite.fetch(creatQuery()))
        .filter(results -> biggestActions.stream().noneMatch(biggestAction -> biggestAction.containsAll(results.getActions())))
        .doOnNext(results ->
            System.out.println(results.getActions().stream().map(BaseLabel::toString).collect(Collectors.joining(", "))))
        .doOnNext(results -> biggestActions.add(results.getActions()));

    outputSQLite.insert(baseResults)
        .andThen(inputSQLite.close())
        .andThen(outputSQLite.close())
        .blockingAwait();
    presenter.show("finish");
  }

  private Query<BaseResult> creatQuery() {
    return SQLiteQuery.select(BaseResult.class)
        .from(BaseResult.class)
        .orderBy("action_size", false)
        .build();
  }
}

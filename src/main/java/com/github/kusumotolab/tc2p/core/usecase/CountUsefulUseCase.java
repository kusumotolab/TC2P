package com.github.kusumotolab.tc2p.core.usecase;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.core.configuration.CountUsefulConfiguration;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.MiningResult.UsefulState;
import com.github.kusumotolab.tc2p.core.entities.Tag;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery.Builder;
import io.reactivex.schedulers.Schedulers;

public class CountUsefulUseCase<V extends View, P extends Presenter<V>> extends UseCase<CountUsefulConfiguration, V, P> {

  public CountUsefulUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final CountUsefulConfiguration countUsefulConfiguration) {
    final List<Tag> tags = countUsefulConfiguration.getTags();
    final SQLite sqLite = new SQLite(countUsefulConfiguration.getInputFilePath().toString());

    final List<MiningResult> miningResults = sqLite.connect()
        .andThen(sqLite.fetch(createQuery(countUsefulConfiguration)))
        .observeOn(Schedulers.single())
        .filter(result -> result.getTags().containsAll(tags))
        .take(countUsefulConfiguration.getCount())
        .toList()
        .blockingGet();

    final Map<UsefulState, List<MiningResult>> stateMap = miningResults.stream()
        .collect(Collectors.groupingBy(MiningResult::getUsefulState));
    final Consumer<UsefulState> consumer = state -> {
      final List<MiningResult> results = stateMap.get(state);
      if (results == null) {
        return;
      }
      presenter.show(state.toString() + ": " + results.size());
    };

    consumer.accept(UsefulState.NONE);
    consumer.accept(UsefulState.NOT_USEFUL);
    consumer.accept(UsefulState.USEFUL);
  }


  private Query<MiningResult> createQuery(final CountUsefulConfiguration configuration) {
    Builder<MiningResult> queryBuilder = SQLiteQuery.select(MiningResult.class).from(MiningResult.class);

    switch (configuration.getSort()) {
      case NODE_SIZE:
        queryBuilder = queryBuilder.orderBy("size", !configuration.isReverse());
        break;
      case ACTION_SIZE:
        queryBuilder = queryBuilder.orderBy("action_size", !configuration.isReverse());
        break;
      case INDEX:
        queryBuilder = queryBuilder.orderBy("id", !configuration.isReverse());
        break;
      case DEPTH:
        queryBuilder = queryBuilder.orderBy("max_depth", !configuration.isReverse());
        break;
      case FREQUENCY:
        queryBuilder = queryBuilder.orderBy("frequency", !configuration.isReverse());
    }

    return queryBuilder.build();
  }

}

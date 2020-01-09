package com.github.kusumotolab.tc2p.core.usecase;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.github.kusumotolab.sdl4j.util.CommandLine;
import com.github.kusumotolab.tc2p.core.configuration.ViewerConfiguration;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.Tag;
import com.github.kusumotolab.tc2p.core.presenter.IViewPresenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery.Builder;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteRelationalCondition;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteRelationalCondition.RelationalOperator;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class ViewerUseCase<V extends View, P extends IViewPresenter<V>> extends IViewUseCase<V, P> {

  private List<MiningResult> miningResults = Lists.newArrayList();
  private int index = 0;
  private SQLite sqLite;

  public ViewerUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final ViewerConfiguration viewerConfiguration) {
    final List<Tag> tags = viewerConfiguration.getTags();
    sqLite = new SQLite(viewerConfiguration.getInputFilePath().toString());
    final Map<Tag, Integer> map = Maps.newHashMap();

    this.miningResults = sqLite.connect()
        .andThen(sqLite.fetch(createQuery(viewerConfiguration)))
        .observeOn(Schedulers.single())
        .doOnNext(result -> {
          for (final Tag tag : result.getTags()) {
            final Integer counter = map.getOrDefault(tag, 0);
            map.put(tag, counter + 1);
          }
        })
        .filter(result -> result.getTags().containsAll(tags))
        .toList()
        .blockingGet();

    for (final Entry<Tag, Integer> entry : map.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }

    if (miningResults.isEmpty()) {
      return;
    }
    index = 0;
    presenter.show(this.miningResults.get(0), index);
    presenter.observeInput();
  }

  private Query<MiningResult> createQuery(final ViewerConfiguration configuration) {
    Builder<MiningResult> queryBuilder = SQLiteQuery.select(MiningResult.class).from(MiningResult.class);

    if (configuration.isShowOnlyCommented()) {
      queryBuilder = queryBuilder.where(new SQLiteRelationalCondition("comment", RelationalOperator.NOT_EQUAL, ""));
    } else if (!configuration.isShowAll()) {
      queryBuilder = queryBuilder.where(new SQLiteRelationalCondition("is_deleted", RelationalOperator.NOT_EQUAL, "1"));
    }

    switch (configuration.getSort()) {
      case SIZE:
        queryBuilder = queryBuilder.orderBy("size", !configuration.isReverse());
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

  @Override
  public void next() {
    if (miningResults.isEmpty()) {
      return;
    }
    if (miningResults.size() == index + 1) {
      return;
    }
    index += 1;
    presenter.show(miningResults.get(index), index);
  }

  @Override
  public void previous() {
    if (miningResults.isEmpty()) {
      return;
    }
    if (index == 0) {
      return;
    }
    index -= 1;
    presenter.show(miningResults.get(index), index);

  }

  @Override
  public void open() {
    final List<String> urls = miningResults.get(index).getUrls();
    urls.stream()
        .distinct()
        .forEach(url -> {
          final CommandLine commandLine = new CommandLine();
          Try.force(() -> commandLine.execute("open", url));
        });
  }

  @Override
  public void delete() {
    final MiningResult result = miningResults.get(index);
    result.setDeleted(true);
    sqLite.update(Observable.just(result)).blockingAwait();

    miningResults.remove(index);
    if (miningResults.isEmpty()) {
      finish();
      return;
    }
    final MiningResult miningResult = miningResults.get(index);
    presenter.show(miningResult, index);
  }

  @Override
  public void finish() {
    sqLite.close().subscribe(presenter::finish);
  }

  @Override
  public void addComment(final String comment) {
    final MiningResult miningResult = miningResults.get(index);
    miningResult.setComment(comment);
    sqLite.update(Observable.just(miningResult))
        .blockingAwait();
    next();
  }
}

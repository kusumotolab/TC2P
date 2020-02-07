package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.core.entities.BaseResult;
import com.github.kusumotolab.tc2p.core.entities.PatternPosition;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteOrder.Order;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import com.github.kusumotolab.tc2p.utils.RxCommandLine;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.common.collect.Sets;

public class BaseViewerUseCase<V extends View, P extends Presenter<V>> extends IBaseViewerUseCase<V, P> {

  private final int HEAD = 1000;

  public BaseViewerUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final Random random = new Random(0);
    final Scanner scanner = new Scanner(System.in);

    final List<BaseResult> baseResults = fetch(input.getDbPath(), HEAD);

    for (int i = 0; i < 1000; i++) {
      final int index = random.nextInt(HEAD);
      final BaseResult baseResult = baseResults.get(index);
      presenter.show("index = " + i);
      presenter.show("project_name = " + baseResult.getProjectName());
      presenter.show("frequency = " + baseResult.getFrequency());
      presenter.show("");

      baseResult.getActions().stream().sorted(Comparator.comparingInt(e -> e.getAction().getPriority()))
          .forEach(action -> presenter.show(action.getAction().toStringWithColor() + " " + action.getType()));

      presenter.show("");

      final Set<String> urls = Sets.newHashSet();
      final List<PatternPosition> sortedPositions = baseResult.getPatternPositions().stream()
          .sorted(Comparator.comparing(PatternPosition::getUrl))
          .collect(Collectors.toList());
      String next = scanner.next();
      while (next.equals("open")) {
        for (int pi = 0; pi < Math.min(10, sortedPositions.size()); pi++) {
          final PatternPosition position = sortedPositions.get(pi);

          if (!urls.contains(position.getUrl())) {
            presenter.show(position.getUrl());
            urls.add(position.getUrl());
          }
          open(position);
          presenter.show(pi + ": " + position.getMjavaDiff());
          Try.lambda(() -> Thread.sleep(10));
        }
        next = scanner.next();
      }
    }
    scanner.close();
  }

  private int countSize(final Path path) {
    final SQLite sqLite = new SQLite(path);
    Query<Integer> query = SQLiteQuery.selectInteger().column("COUNT(*)").from(BaseResult.class).build();
    final Integer size = sqLite.connect()
        .andThen(sqLite.fetch(query))
        .blockingFirst();
    sqLite.close().blockingAwait();
    return size;
  }

  private List<BaseResult> fetch(final Path path, int limit) {
    final SQLite sqLite = new SQLite(path);

    final List<BaseResult> baseResults = sqLite.connect()
        .andThen(sqLite.fetch(SQLiteQuery.select(BaseResult.class)
            .from(BaseResult.class)
            .orderBy("action_size", Order.DESC)
            .limit(limit)
            .build()))
        .toList()
        .blockingGet();
    sqLite.close().blockingAwait();
    return baseResults;
  }

  private void open(final PatternPosition position) {
    final RxCommandLine commandLine = new RxCommandLine();
    commandLine.execute("open", position.getUrl()).blockingGet();
  }
}

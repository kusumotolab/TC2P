package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;
import com.github.kusumotolab.tc2p.core.entities.BaseLabel;
import com.github.kusumotolab.tc2p.core.entities.BaseResult;
import com.github.kusumotolab.tc2p.core.entities.PatternPosition;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import com.github.kusumotolab.tc2p.utils.RxCommandLine;
import com.github.kusumotolab.tc2p.utils.Try;

public class BaseViewerUseCase<V extends View, P extends Presenter<V>> extends IBaseViewerUseCase<V, P> {

  public BaseViewerUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final int size = countSize(input.getDbPath());

    final Random random = new Random(0);
    final Scanner scanner = new Scanner(System.in);

    for (int i = 0; i < size; i++) {
      final int index = random.nextInt(size);
      final BaseResult baseResult = fetch(input.getDbPath(), index);
      presenter.show("index = " + i);
      presenter.show("project_name = " + baseResult.getProjectName());
      for (final BaseLabel action : baseResult.getActions()) {
        presenter.show(action.getAction().toStringWithColor() + " " + action.getType());
      }

      for (int pi = 0; pi < baseResult.getPatternPositions().size(); pi++) {
        final PatternPosition position = baseResult.getPatternPositions().get(pi);
        open(position);
        presenter.show(pi + ": " + position.getMjavaDiff());
        Try.lambda(() -> Thread.sleep(10));
      }

      scanner.next();
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

  private BaseResult fetch(final Path path, int index) {
    final SQLite sqLite = new SQLite(path);

    final BaseResult baseResult = sqLite.connect()
        .andThen(sqLite.fetch(SQLiteQuery.select(BaseResult.class)
            .from(BaseResult.class)
            .offset(index)
            .limit(1)
            .build()))
        .blockingFirst();
    sqLite.close().blockingAwait();
    return baseResult;
  }

  private void open(final PatternPosition position) {
    final RxCommandLine commandLine = new RxCommandLine();
    commandLine.execute("open", position.getUrl()).blockingGet();
  }
}

package com.github.kusumotolab.tc2p.core.usecase;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.jgrapht.graph.AbstractBaseGraph;
import com.github.kusumotolab.tc2p.core.entities.BaseLabel;
import com.github.kusumotolab.tc2p.core.entities.BaseResult;
import com.github.kusumotolab.tc2p.core.entities.PatternPosition;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
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
    final List<Path> dbPaths = Arrays.stream(Objects.requireNonNull(input.getDbDirPath().toFile().listFiles()))
        .filter(e -> e.getName().endsWith("sqlite"))
        .map(File::toPath)
        .collect(Collectors.toList());

    final List<BaseResult> results = dbPaths.stream()
        .flatMap(e -> fetch(e).stream())
        .collect(Collectors.toList());

    final int size = results.size();
    final Random random = new Random(0);
    final Scanner scanner = new Scanner(System.in);

    for (int i = 0; i < size; i++) {
      final int index = random.nextInt(size);
      final BaseResult baseResult = results.get(index);
      presenter.show("index = " + index);
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

  private List<BaseResult> fetch(final Path path) {
    final SQLite sqLite = new SQLite(path);

    final List<BaseResult> baseResults = sqLite.connect()
        .andThen(sqLite.fetch(SQLiteQuery.select(BaseResult.class)
            .from(BaseResult.class)
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

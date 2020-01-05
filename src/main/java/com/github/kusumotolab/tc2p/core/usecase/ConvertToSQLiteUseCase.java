package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Files;
import java.util.List;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningEditPatternResultParser;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.utils.Try;
import io.reactivex.Observable;

public class ConvertToSQLiteUseCase<V extends View, P extends Presenter<V>> extends IConvertUseCase<V, P> {

  public ConvertToSQLiteUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final List<String> allLines = Try.force(() -> Files.readAllLines(input.getInputPath()));
    final MiningEditPatternResultParser.Input parserInput = new MiningEditPatternResultParser.Input(allLines);
    final List<MiningResult> miningResults = new MiningEditPatternResultParser().execute(parserInput);

    final SQLite sqLite = new SQLite(input.getOutputPath().toString());
    sqLite.connect()
        .andThen(sqLite.createTable(MiningResult.class))
        .andThen(sqLite.insert(Observable.fromIterable(miningResults)))
        .andThen(sqLite.close())
        .blockingAwait();
  }
}

package com.github.kusumotolab.tc2p.tools.db.sqlite.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class SQLiteUpdateExecutor extends SQLCommandExecutor {

  public SQLiteUpdateExecutor(final SQLite sqLite) {
    super(sqLite);
  }

  public <Model extends SQLiteObject> Completable update(final Observable<Model> observable) {
    return observable.flatMapCompletable(model -> Completable.create(emitter -> {
      final boolean hasPrimaryKey = model.getColumns().stream().anyMatch(e -> e.getValue().primaryKey());
      if (!hasPrimaryKey) {
        emitter.onError(new IllegalArgumentException("model has not primary key"));
        return;
      }
      final Connection connection = sqLite.getConnection();
      connection.setAutoCommit(false);

      final String prepareStatementCommand = model.prepareUpdateStatementCommand();
      System.out.println(prepareStatementCommand);

      final PreparedStatement prepareStatement = connection.prepareStatement(prepareStatementCommand);
      model.addBatchUpdateCommand(prepareStatement);

      prepareStatement.executeBatch();
      connection.commit();
      prepareStatement.close();
      emitter.onComplete();
    }).subscribeOn(Schedulers.single()));
  }
}

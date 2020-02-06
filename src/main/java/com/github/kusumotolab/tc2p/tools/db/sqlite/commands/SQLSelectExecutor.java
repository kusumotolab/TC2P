package com.github.kusumotolab.tc2p.tools.db.sqlite.commands;

import java.sql.ResultSet;
import java.sql.Statement;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLSelectExecutor<Model> extends SQLCommandExecutor {

  public SQLSelectExecutor(final SQLite sqLite) {
    super(sqLite);
  }

  public Observable<Model> execute(final Query<Model> query, final int fetchSize) {
    return Observable.create(emitter -> {
      final Statement statement = sqLite.getConnection()
          .createStatement();
      statement.setFetchSize(fetchSize);
      final String command = query.toCommand();
      log.debug(command);
      final ResultSet resultSet = statement.executeQuery(command);
      while (resultSet.next()) {
        final Model object = query.resolve(resultSet);
        emitter.onNext(object);
      }
      emitter.onComplete();
    });
  }
}

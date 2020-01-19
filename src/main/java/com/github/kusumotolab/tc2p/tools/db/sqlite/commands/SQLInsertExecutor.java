package com.github.kusumotolab.tc2p.tools.db.sqlite.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLInsertExecutor extends SQLCommandExecutor {

  public SQLInsertExecutor(final SQLite sqLite) {
    super(sqLite);
  }

  public <Model extends SQLiteObject> Completable execute(final Observable<Model> observer, final int bufferSize) {
    return Completable.create(emitter -> observer
        .subscribeOn(Schedulers.single()).buffer(bufferSize)
        .doOnNext(list -> {
          if (list.isEmpty()) {
            return;
          }
          log.debug("Insert " + list.size() + " Objects.");
          final Connection connection = sqLite.getConnection();
          connection.setAutoCommit(false);
          final SQLiteObject sampleObject = list.get(0);
          final String prepareStatementCommand = sampleObject.prepareStatementCommand();
          final PreparedStatement prepareStatement = connection.prepareStatement(prepareStatementCommand);

          for (final SQLiteObject object : list) {
            object.addBatchCommand(prepareStatement);
          }
          prepareStatement.executeBatch();
          connection.commit();
        })
        .subscribe(e -> {
        }, Throwable::printStackTrace, emitter::onComplete))
        .subscribeOn(Schedulers.io());
  }
}

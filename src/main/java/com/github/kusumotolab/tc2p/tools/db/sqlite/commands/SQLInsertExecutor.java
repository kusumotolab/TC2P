package com.github.kusumotolab.tc2p.tools.db.sqlite.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLInsertExecutor<Model extends SQLiteObject> extends SQLCommandExecutor {

  public SQLInsertExecutor(final SQLite sqLite) {
    super(sqLite);
  }

  public Completable execute(final Observable<Model> observer, final int bufferSize) {
    return Completable.fromObservable(observer.buffer(bufferSize)
            .doOnNext(list -> {
              if (list.isEmpty()) {
                return;
              }

              final Connection connection = sqLite.getConnection();
              connection.setAutoCommit(false);
              final SQLiteObject sampleObject = list.get(0);
              final String prepareStatementCommand = sampleObject.prepareStatementCommand();
              final PreparedStatement prepareStatement = connection.prepareStatement(
                  prepareStatementCommand);

              for (final SQLiteObject object : list) {
                object.addBatchCommand(prepareStatement);
              }
              prepareStatement.executeBatch();
              connection.commit();
              log.debug("Insert " + list.size() + " Objects.");
            })).subscribeOn(Schedulers.single());
  }
}

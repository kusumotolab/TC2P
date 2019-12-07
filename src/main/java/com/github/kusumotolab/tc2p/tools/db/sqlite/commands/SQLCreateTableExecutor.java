package com.github.kusumotolab.tc2p.tools.db.sqlite.commands;

import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.tools.db.sqlite.Column;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import io.reactivex.Completable;

public class SQLCreateTableExecutor extends SQLCommandExecutor {

  public SQLCreateTableExecutor(final SQLite sqLite) {
    super(sqLite);
  }

  public <Model extends SQLiteObject> Completable execute(final Class<Model> modelClass) {
    return Completable.create(emitter -> {
      final List<String> columns = SQLiteObject.getColumns(modelClass)
          .stream()
          .map(Column::toString)
          .collect(Collectors.toList());

      final String command = "CREATE TABLE IF NOT EXISTS "
          + SQLiteObject.getDBName(modelClass)
          + " ("
          + String.join(", ", columns)
          + ")";
      final Statement statement = sqLite.getConnection()
          .createStatement();
      statement.executeUpdate(command);
      emitter.onComplete();
    });
  }
}

package com.github.kusumotolab.tc2p.tools.db.sqlite.commands;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.tools.db.sqlite.Column;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteColumn;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.Completable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLCreateTableExecutor extends SQLCommandExecutor {

  public SQLCreateTableExecutor(final SQLite sqLite) {
    super(sqLite);
  }

  public <Model extends SQLiteObject> Completable execute(final Class<Model> modelClass) {
    return Completable.create(emitter -> {
      createTable(modelClass);
      createIndexes(modelClass);
      emitter.onComplete();
    });
  }

  private <Model extends SQLiteObject> void createTable(final Class<Model> modelClass)
      throws SQLException {
    final List<Column> columns = SQLiteObject.getColumns(modelClass);
    final List<String> primaryKeys = columns.stream()
        .filter(column -> column.getValue()
            .primaryKey())
        .map(e -> e.getName())
        .collect(Collectors.toList());

    final List<String> columnNames = columns
        .stream()
        .map(column -> {
          final SQLiteColumn value = column.getValue();
          String columnText = column.toString();
          if (value.primaryKey() && primaryKeys.size() == 1) {
            columnText += " PRIMARY KEY";
          }
          if (value.autoIncrement()) {
            columnText += " AUTOINCREMENT";
          }
          return columnText;
        })
        .collect(Collectors.toList());

    String command = "CREATE TABLE IF NOT EXISTS "
        + SQLiteObject.getDBName(modelClass)
        + " ("
        + String.join(", ", columnNames);
    if (2 <= primaryKeys.size()) {
      command += ", PRIMARY KEY ("
          + String.join(",", primaryKeys)
          + ")";
    }
    command += ")";
    log.debug(command);

    final Statement statement = sqLite.getConnection()
        .createStatement();
    statement.executeUpdate(command);
  }

  private <Model extends SQLiteObject> void createIndexes(final Class<Model> modelClass)
      throws SQLException {
    final Multimap<String, Column> indexIdMap = HashMultimap.create();
    SQLiteObject.getColumns(modelClass)
        .forEach(column -> {
          final SQLiteColumn sqLiteColumn = column.getValue();
          for (final String s : sqLiteColumn.indexIds()) {
            indexIdMap.put(s, column);
          }
        });
    final Map<String, Collection<Column>> map = indexIdMap.asMap();
    for (final Entry<String, Collection<Column>> entry : map.entrySet()) {
      final String indexName = entry.getKey();
      final String columnsString = entry.getValue()
          .stream()
          .map(Column::getName)
          .sorted()
          .collect(Collectors.joining(", "));
      final String indexCommand = "CREATE INDEX IF NOT EXISTS "
          + indexName
          + " ON "
          + SQLiteObject.getDBName(modelClass)
          + " ("
          + columnsString
          + " );";
      final Statement indexStatement = sqLite.getConnection()
          .createStatement();
      indexStatement.executeUpdate(indexCommand);
    }
  }
}

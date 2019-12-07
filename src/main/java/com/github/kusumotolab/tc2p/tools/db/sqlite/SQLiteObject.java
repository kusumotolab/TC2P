package com.github.kusumotolab.tc2p.tools.db.sqlite;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.kusumotolab.tc2p.tools.db.DBObject;

public abstract class SQLiteObject extends DBObject {

  public String getDBName() {
    return getDBName(getClass());
  }

  public static String getDBName(final Class<? extends SQLiteObject> aClass) {
    final String fqn = aClass.getName();
    final String[] split = fqn.split("\\.");
    return split[split.length - 1];
  }

  public List<Column> getColumns() {
    return getColumns(getClass());
  }

  public static List<Column> getColumns(final Class<? extends SQLiteObject> aClass) {
    return Stream.of(aClass.getDeclaredFields())
        .filter(e -> e.getAnnotation(SQLiteColumn.class) != null)
        .map(Column::new)
        .collect(Collectors.toList());
  }

  public void addBatchCommand(final PreparedStatement preparedStatement)
      throws SQLException, IllegalAccessException {
    final List<Column> columns = getColumns();
    for (int i = 0; i < columns.size(); i++) {
      final Column column = columns.get(i);
      final SQLiteColumn sqLiteColumn = column.getValue();
      final Field field = column.getField();
      field.setAccessible(true);
      final Object value = sqLiteColumn.autoIncrement() ? null : field.get(this);
      preparedStatement.setObject(i + 1, encodeField(value ,field));
    }
    preparedStatement.addBatch();
  }

  protected Object encodeField(final Object value, final Field field) {
    return value;
  }

  public String prepareStatementCommand() {
    final StringBuilder stringBuilder = new StringBuilder("INSERT OR IGNORE INTO ").append(
        getDBName())
        .append(" VALUES(");
    final List<String> columns = getColumns().stream()
        .map(Column::getValue)
        .map(e -> {
          if (e.primaryKey() && e.autoIncrement()) {
            return "$next_id";
          }
          return "?";
        })
        .collect(Collectors.toList());
    stringBuilder.append(String.join(", ", columns))
        .append(")");
    return stringBuilder.toString();
  }

  public void decode(final ResultSet resultSet) throws SQLException, IllegalAccessException {
    final List<Column> columns = getColumns();
    for (final Column column : columns) {
      final String name = column.getName();
      final Object value = resultSet.getObject(name);
      final Field field = column.getField();
      field.setAccessible(true);

      final Object decodedValue = decodeField(value, field);
      field.set(this, decodedValue);
    }
  }

  protected Object decodeField(final Object value, final Field field) {
    return value;
  }
}

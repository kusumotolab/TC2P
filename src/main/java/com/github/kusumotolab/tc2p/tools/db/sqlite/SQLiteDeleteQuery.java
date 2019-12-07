package com.github.kusumotolab.tc2p.tools.db.sqlite;

import com.github.kusumotolab.tc2p.tools.db.Query;

public class SQLiteDeleteQuery<T> implements Query<T> {

  public static final String COMMAND = "DELETE";

  private final String dbName;
  private final SQLiteCondition condition;

  public SQLiteDeleteQuery(final String dbName, final SQLiteCondition condition) {
    this.dbName = dbName;
    this.condition = condition;
  }

  @Override
  public String toCommand() {
    final StringBuilder stringBuilder = new StringBuilder(COMMAND).append(" FROM ")
        .append(dbName);
    if (condition != null) {
      final SQLiteCondition condition = this.condition;
      stringBuilder.append(" WHERE ")
          .append(condition.toString());
    }
    stringBuilder.append(";");
    return stringBuilder.toString();
  }

  @Override
  public T resolve(final Object object) {
    throw new UnsupportedOperationException();
  }
}

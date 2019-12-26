package com.github.kusumotolab.tc2p.tools.db.sqlite;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SQLiteLikeCondition extends SQLiteCondition {

  private final String field;
  private final String pattern;

  @Override
  public String toConditionStatement() {
    return field + " LIKE '" + pattern + "'";
  }
}

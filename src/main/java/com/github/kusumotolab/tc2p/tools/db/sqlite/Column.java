package com.github.kusumotolab.tc2p.tools.db.sqlite;

import java.lang.reflect.Field;
import java.sql.JDBCType;

public class Column {

  private final Field field;
  private final SQLiteColumn value;

  public Column(final Field field) {
    this.field = field;
    this.value = field.getAnnotation(SQLiteColumn.class);
  }

  public Field getField() {
    return field;
  }

  public SQLiteColumn getValue() {
    return value;
  }

  public String getName() {
    final SQLiteColumn sqLiteColumn = getValue();
    final String name = sqLiteColumn.name();
    if (!name.equals(SQLiteColumn.EMPTY)) {
      return name;
    }
    return field.getName();
  }

  @Override
  public String toString() {
    final SQLiteColumn value = getValue();
    final JDBCType type = JDBCType.valueOf(value.type());
    return getName() + " " + type.getName();
  }
}

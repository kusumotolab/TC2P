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
    if (!name.equals(SQLiteColumn.EMPTY_NAME)) {
      return name;
    }
    return field.getName();
  }

  @Override
  public String toString() {
    final SQLiteColumn value = getValue();
    final JDBCType type = JDBCType.valueOf(value.type());
    final StringBuilder stringBuilder = new StringBuilder(getName()).append(" ")
        .append(type.getName());
    if (value.primaryKey()) {
      stringBuilder.append(" PRIMARY KEY");
    }

    if (value.autoIncrement()) {
      stringBuilder.append(" AUTOINCREMENT");
    }
    return stringBuilder.toString();
  }
}

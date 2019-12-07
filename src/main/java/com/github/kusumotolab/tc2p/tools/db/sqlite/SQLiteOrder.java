package com.github.kusumotolab.tc2p.tools.db.sqlite;

public class SQLiteOrder {

  private final String column;
  private final Order order;

  public SQLiteOrder(final String column, final Order order) {
    this.column = column;
    this.order = order;
  }

  public String getColumn() {
    return column;
  }

  public Order getOrder() {
    return order;
  }

  public enum Order {
    ASC, DESC
  }
}

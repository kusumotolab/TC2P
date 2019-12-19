package com.github.kusumotolab.tc2p.tools.db.sqlite;

import lombok.Data;

@Data
public class SQLiteOrder {

  private final String column;
  private final Order order;

  public enum Order {
    ASC, DESC
  }
}

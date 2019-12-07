package com.github.kusumotolab.tc2p.tools.db.sqlite.commands;

import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;

public abstract class SQLCommandExecutor {

  protected final SQLite sqLite;

  public SQLCommandExecutor(final SQLite sqLite) {
    this.sqLite = sqLite;
  }
}

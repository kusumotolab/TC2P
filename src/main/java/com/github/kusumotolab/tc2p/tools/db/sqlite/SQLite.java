package com.github.kusumotolab.tc2p.tools.db.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.kusumotolab.tc2p.tools.db.DB;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.commands.SQLCreateTableExecutor;
import com.github.kusumotolab.tc2p.tools.db.sqlite.commands.SQLInsertExecutor;
import com.github.kusumotolab.tc2p.tools.db.sqlite.commands.SQLSelectExecutor;
import com.github.kusumotolab.tc2p.tools.db.sqlite.commands.SQLiteUpdateExecutor;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class SQLite implements DB<SQLiteObject> {

  private static final String DEFAULT_SQLITE_PATH = "./sqlite3.db";
  private static boolean isInitialized = false;
  private static final Logger log = LoggerFactory.getLogger(SQLite.class);

  private final String sqliteURL;
  private Connection connection;

  final SQLInsertExecutor insertExecutor = new SQLInsertExecutor(this);

  public SQLite() {
    this(DEFAULT_SQLITE_PATH);
  }

  public SQLite(final Path sqlitePath) {
    this(sqlitePath.toAbsolutePath().toString());
  }

  public SQLite(final String sqlitePath) {
    this.sqliteURL = "jdbc:sqlite:" + sqlitePath;

    if (!isInitialized) {
      initialize();
      isInitialized = true;
    }
  }

  private void initialize() {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (final ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Connection getConnection() {
    return this.connection;
  }

  @Override
  public Completable connect() {
    return Completable.create(emitter -> {
      if (this.connection == null) {
        this.connection = DriverManager.getConnection(sqliteURL, getProperties());
        log.trace("Connection Success: " + sqliteURL);
      }
      emitter.onComplete();
    });
  }

  private static Properties getProperties() {
    Properties prop = new Properties();
    prop.put("journal_mode", "MEMORY");
    prop.put("sync_mode", "OFF");
    return prop;
  }

  @Override
  public Completable close() {
    return Completable.create(emitter -> {
      if (this.connection != null) {
        this.connection.close();
        this.connection = null;
        log.trace("Connection is closed");
      }
      emitter.onComplete();
    });
  }

  public <Model extends SQLiteObject> Completable createTable(final Class<Model> modelClass) {
    return new SQLCreateTableExecutor(this).execute(modelClass);
  }

  public <Model extends SQLiteObject> Completable insert(final Observable<Model> observer) {
    return insert(observer, 10000);
  }

  public <Model extends SQLiteObject> Completable insert(final Observable<Model> observer,
      final int bufferSize) {
    return insertExecutor.execute(observer, bufferSize);
  }

  @Override
  public <Model extends SQLiteObject> Completable update(final Observable<Model> object) {
    return new SQLiteUpdateExecutor(this).update(object);
  }

  @Override
  public <Model extends SQLiteObject> Observable<Model> fetch(final Single<Query<Model>> single) {
    return fetch(single, 1000000);
  }

  public <Model extends SQLiteObject> Observable<Model> fetch(final Query<Model> query) {
    return fetch(query, 1000000);
  }

  public <Model extends SQLiteObject> Observable<Model> fetch(final Single<Query<Model>> single,
      final int fetchSize) {
    return single.flatMapObservable(query -> fetch(query, fetchSize));
  }

  public <Model extends SQLiteObject> Observable<Model> fetch(final Query<Model> query,
      final int fetchSize) {
    return new SQLSelectExecutor<Model>(this).execute(query, fetchSize);
  }

  @Override
  public <Model extends SQLiteObject> Completable delete(final Single<Query<Model>> single) {
    throw new UnsupportedOperationException();
  }
}

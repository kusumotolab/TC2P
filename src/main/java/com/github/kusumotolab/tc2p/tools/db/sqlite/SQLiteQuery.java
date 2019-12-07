package com.github.kusumotolab.tc2p.tools.db.sqlite;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteOrder.Order;
import com.github.kusumotolab.tc2p.utils.Try;
import io.reactivex.Single;

public class SQLiteQuery {

  public static <Q> Builder<Q> select(final Function<ResultSet, Q> mapper) {
    return new Builder<Q>(SQLiteSelectQuery.COMMAND)
        .mapper(mapper);
  }

  public static <T extends SQLiteObject> Builder<T> select(final Supplier<T> supplier) {
    return new Builder<T>(SQLiteSelectQuery.COMMAND)
        .mapper(resultSet -> Try.force(() ->{
          final T object = supplier.get();
          object.decode(resultSet);
          return object;
        }));
  }

  public static <T extends SQLiteObject> Builder<T> select(final Class<T> tClass) {
    return select(() -> Try.force(() -> tClass.getDeclaredConstructor().newInstance()));
  }

  public static Builder<String> selectString() {
    return select(resultSet -> Try.force(() -> resultSet.getString(1)));
  }

  public static Builder<Integer> selectInteger() {
    return select(resultSet -> Try.force(() -> resultSet.getInt(1)));
  }

  public static <T extends SQLiteObject> Builder<T> deleteFrom(final Class<T> tClass) {
    return new Builder<T>(SQLiteDeleteQuery.COMMAND).from(tClass);
  }

  public static <T extends SQLiteObject> Builder<T> deleteFrom(final String dbName) {
    return new Builder<T>(SQLiteDeleteQuery.COMMAND).from(dbName);
  }

  public static class Builder<R> {

    private final String command;
    private final List<String> columns = new ArrayList<>();
    private String dbName;
    private SQLiteCondition where;
    private Integer limit;
    private Integer offset;
    private final List<SQLiteOrder> orders = new ArrayList<>();
    private final List<String> groupsBys = new ArrayList<>();
    private SQLiteCondition having;

    private Function<ResultSet, R> mapper = e -> null;

    private Builder(final String command) {
      this.command = command;
    }

    public Query<R> build() {
      switch (command) {
        case SQLiteSelectQuery.COMMAND:
          return new SQLiteSelectQuery<R>(dbName, columns, where, limit, offset, orders, groupsBys,
              having, mapper);
        case SQLiteDeleteQuery.COMMAND:
          return new SQLiteDeleteQuery<>(dbName, where);
      }
      throw new UnsupportedOperationException();
    }

    public Single<Query<R>> toSingle() {
      return Single.just(build());
    }

    public Builder<R> column(final String name) {
      columns.add(name);
      return this;
    }

    public Builder<R> columns(final List<String> columns) {
      this.columns.addAll(columns);
      return this;
    }

    public Builder<R> from(final String dbName) {
      this.dbName = dbName;
      return this;
    }

    public <T extends SQLiteObject> Builder<R> from(final Class<T> tClass) {
      this.dbName = SQLiteObject.getDBName(tClass);
      return this;
    }

    public Builder<R> where(final SQLiteCondition condition) {
      this.where = condition;
      return this;
    }

    public Builder<R> limit(final int limit) {
      this.limit = limit;
      return this;
    }

    public Builder<R> offset(final int offset) {
      this.offset = offset;
      return this;
    }

    public Builder<R> orderBy(final String column) {
      return orderBy(column, true);
    }

    public Builder<R> orderBy(final String column, final boolean asc) {
      orders.add(new SQLiteOrder(column, asc ? Order.ASC : Order.DESC));
      return this;
    }

    public Builder<R> groupBy(final List<String> groupsBys) {
      this.groupsBys.addAll(groupsBys);
      return this;
    }

    public Builder<R> groupBy(final String groupsBy) {
      this.groupsBys.add(groupsBy);
      return this;
    }

    public Builder<R> having(final SQLiteCondition condition) {
      this.having = condition;
      return this;
    }

    public Builder<R> mapper(final Function<ResultSet, R> mapper) {
      this.mapper = mapper;
      return this;
    }
  }
}

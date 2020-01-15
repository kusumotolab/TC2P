package com.github.kusumotolab.tc2p.tools.db.sqlite;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.tools.db.Query;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SQLiteSelectQuery<T> implements Query<T> {

  public static final String COMMAND = "SELECT";

  private final String dbName;
  private final List<String> columns;
  private final SQLiteCondition where;
  private final Integer limit;
  private final Integer offset;
  private final List<SQLiteOrder> orders;
  private final List<String> groupsBys;
  private final SQLiteCondition having;
  private final Function<ResultSet, T> mapper;

  @Override
  public String toCommand() {
    final String columns = this.columns.isEmpty() ? "*" : String.join(", ", this.columns);
    final StringBuilder stringBuilder = new StringBuilder(COMMAND)
        .append(" ")
        .append(columns)
        .append(" FROM ")
        .append(dbName);

    if (where != null) {
      stringBuilder.append(" WHERE ")
          .append(where.toString());
    }

    if (!orders.isEmpty()) {
      final List<String> orders = this.orders.stream()
          .map(e -> e.getColumn() + " " + e.getOrder())
          .collect(Collectors.toList());
      stringBuilder.append(" ORDER BY ")
          .append(String.join(", ", orders));
    }

    if (limit != null) {
      stringBuilder.append(" LIMIT ")
          .append(limit);
    }

    if (offset != null) {
      stringBuilder.append(" OFFSET ")
          .append(offset);
    }

    if (!groupsBys.isEmpty()) {
      stringBuilder.append(" GROUP BY ")
          .append(String.join(", ", groupsBys));
    }

    if (having != null) {
      stringBuilder.append(" HAVING ")
          .append(having.toString());
    }

    stringBuilder.append(";");
    return stringBuilder.toString();
  }

  @Override
  public T resolve(final Object object) {
    if (!(object instanceof ResultSet)) {
      throw new IllegalArgumentException("Object is not ResultSet");
    }
    final ResultSet resultSet = (ResultSet) object;
    return mapper.apply(resultSet);
  }
}

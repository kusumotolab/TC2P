package com.github.kusumotolab.tc2p.tools.db.sqlite;

import java.util.ArrayList;
import java.util.List;

public class SQLiteCondition {

  private final String left;
  private final RelationalOperator relationalOperator;
  private final String right;

  private final List<AdditionalCondition> additionalConditions = new ArrayList<>();

  public SQLiteCondition() {
    this("1", RelationalOperator.EQUAL, "1");
  }

  public SQLiteCondition(final Object left,
      final RelationalOperator relationalOperator, final Object right) {
    this.left = left.toString();
    this.relationalOperator = relationalOperator;
    if (right instanceof String) {
      this.right = "\"" + right + "\"";
    } else {
      this.right = right.toString();
    }
  }

  public SQLiteCondition and(final SQLiteCondition condition) {
    additionalConditions.add(new AdditionalCondition(LogicalOperator.AND, condition));
    return this;
  }

  public SQLiteCondition or(final SQLiteCondition condition) {
    additionalConditions.add(new AdditionalCondition(LogicalOperator.OR, condition));
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(left)
        .append(relationalOperator)
        .append(right);

    for (final AdditionalCondition additionalCondition : additionalConditions) {
      final SQLiteCondition condition = additionalCondition.getCondition();
      builder.append(" ")
          .append(additionalCondition.getLogicalOperator())
          .append(" ")
          .append(condition.toString());
    }
    return builder.toString();
  }

  public static class AdditionalCondition {

    private final LogicalOperator logicalOperator;
    private final SQLiteCondition condition;

    public AdditionalCondition(final LogicalOperator logicalOperator,
        final SQLiteCondition condition) {
      this.logicalOperator = logicalOperator;
      this.condition = condition;
    }

    public LogicalOperator getLogicalOperator() {
      return logicalOperator;
    }

    public SQLiteCondition getCondition() {
      return condition;
    }
  }

  public enum LogicalOperator {
    AND, OR // TODO: NOT;
  }

  public enum RelationalOperator {
    EQUAL("="),
    NOT_EQUAL("<>"),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    LESS("<"),
    LESS_OR_EQUAL("<=");

    private final String text;

    RelationalOperator(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return this.text;
    }
  }
}

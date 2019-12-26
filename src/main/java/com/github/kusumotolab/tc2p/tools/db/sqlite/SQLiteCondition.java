package com.github.kusumotolab.tc2p.tools.db.sqlite;

import java.util.ArrayList;
import java.util.List;

public abstract class SQLiteCondition {

  private final List<AdditionalCondition> additionalConditions = new ArrayList<>();

  public SQLiteCondition and(final SQLiteCondition condition) {
    additionalConditions.add(new AdditionalCondition(LogicalOperator.AND, condition));
    return this;
  }

  public SQLiteCondition or(final SQLiteCondition condition) {
    additionalConditions.add(new AdditionalCondition(LogicalOperator.OR, condition));
    return this;
  }

  public abstract String toConditionStatement();

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder().append(toConditionStatement());

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
}

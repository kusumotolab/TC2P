package com.github.kusumotolab.tc2p.tools.db.sqlite;

public class SQLiteRelationalCondition extends SQLiteCondition {

  private final String left;
  private final RelationalOperator relationalOperator;
  private final String right;

  public SQLiteRelationalCondition() {
    this("1", RelationalOperator.EQUAL, "1");
  }

  public SQLiteRelationalCondition(final Object left, final RelationalOperator relationalOperator, final Object right) {
    this.left = left.toString();
    this.relationalOperator = relationalOperator;
    if (right instanceof String) {
      this.right = "\"" + right + "\"";
    } else {
      this.right = right.toString();
    }
  }

  @Override
  public String toConditionStatement() {
    return left
        + relationalOperator
        + right;
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

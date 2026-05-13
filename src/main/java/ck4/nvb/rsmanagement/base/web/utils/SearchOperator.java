package ck4.nvb.rsmanagement.base.web.utils;

import lombok.Getter;

@Getter
public enum SearchOperator {
  EQUALS(":"),
  NOT_IN("<>"),
  IN("><"),
  NEGATION("!"),
  GREATER_THAN(">"),
  GREATER_THAN_OR_EQUAL(">:"),
  LESS_THAN("<"),
  LESS_THAN_OR_EQUAL("<:"),
  BETWEEN("()"),
  CONTAINS("~");

  public final String symbol;

  SearchOperator(String symbol) {
    this.symbol = symbol;
  }

  public static SearchOperator getOperator(final String symbol) {
    return switch (symbol) {
      case ":" -> EQUALS;
      case "!" -> NEGATION;
      case "<>" -> NOT_IN;
      case "><" -> IN;
      case ">" -> GREATER_THAN;
      case "<" -> LESS_THAN;
      case ">:" -> GREATER_THAN_OR_EQUAL;
      case "<:" -> LESS_THAN_OR_EQUAL;
      case "()" -> BETWEEN;
      case "~" -> CONTAINS;
      default -> null;
    };
  }
}
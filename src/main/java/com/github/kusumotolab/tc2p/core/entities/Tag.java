package com.github.kusumotolab.tc2p.core.entities;

import java.util.Optional;

public enum  Tag {
  FOR, ENHANCED_FOR, TRY, WHILE, SWITCH, ANONYMOUS_CLASS_DECLARATION, LAMBDA;

  public static Optional<Tag> create(final String type) {

    switch (type) {
      case "ForStatement":
        return Optional.of(FOR);
      case "EnhancedForStatement":
        return Optional.of(ENHANCED_FOR);
      case "TryStatement":
        return Optional.of(TRY);
      case "WhileStatement":
        return Optional.of(WHILE);
      case "SwitchStatement":
        return Optional.of(SWITCH);
      case "AnonymousClassDeclaration":
        return Optional.of(ANONYMOUS_CLASS_DECLARATION);
      case "LambdaExpression":
        return Optional.of(LAMBDA);
      default:
        return Optional.empty();
    }
  }

  public String getName() {
    switch (this) {
      case FOR:
        return "For";
      case TRY:
        return "Try";
      case ENHANCED_FOR:
        return "Enhanced For";
      case WHILE:
        return "While";
      case SWITCH:
        return "Switch";
      case ANONYMOUS_CLASS_DECLARATION:
        return "Anonymous Class Declaration";
      case LAMBDA:
        return "Lambda";
    }
    return "";
  }
}

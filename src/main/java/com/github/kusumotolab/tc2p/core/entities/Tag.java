package com.github.kusumotolab.tc2p.core.entities;

import java.util.Optional;

public enum  Tag {
  FOR, ENHANCED_FOR, TRY, WHILE, SWITCH;

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
        return "Enhanced_For";
      case WHILE:
        return "While";
      case SWITCH:
        return "Switch";
    }
    return "";
  }
}

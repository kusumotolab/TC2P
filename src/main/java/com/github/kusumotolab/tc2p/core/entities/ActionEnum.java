package com.github.kusumotolab.tc2p.core.entities;

public enum ActionEnum {
  INS, DEL, UPD, SRC_MOV, DST_MOVE;

  public String getColorCode() {
    switch (this) {
      case DEL:
        return "\u001b[00;31m";
      case INS:
        return "\u001b[00;32m";
      case UPD:
        return "\u001b[00;33m";
      case SRC_MOV:
      case DST_MOVE:
        return "\u001b[00;35m";
      default:
        return "";
    }
  }
  public String toStringWithColor() {
    return getColorCode() + name() + "\u001b[00m";
  }
}

package com.github.kusumotolab.tc2p.core.view;

import com.github.kusumotolab.tc2p.framework.View;

public class ConsoleView implements View {

  public void print(final String text) {
    System.out.println(text);
  }
}

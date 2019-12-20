package com.github.kusumotolab.tc2p.core.view;

import com.github.kusumotolab.tc2p.framework.View;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleView implements View {

  public void print(final String text) {
    log.info(text);
  }
}

package com.github.kusumotolab.tc2p.core.view;

import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Widget;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import com.github.kusumotolab.tc2p.core.controller.IViewerController;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.Try;
import lombok.Setter;

public class InteractiveConsoleView implements View {

  private final Terminal terminal = Try.force(TerminalBuilder::terminal);
  private final LineReader reader;
  @Setter private IViewerController<?, ?> controller;

  public InteractiveConsoleView() {
    this.reader = LineReaderBuilder.builder().terminal(terminal).build();

    reader.getKeyMaps().get(LineReader.MAIN).bind((Widget) () -> {
      reader.callWidget(LineReader.CLEAR_SCREEN);
      controller.next();
      return true;
    }, KeyMap.ctrl('j'));

    reader.getKeyMaps().get(LineReader.MAIN).bind((Widget) () -> {
      reader.callWidget(LineReader.CLEAR_SCREEN);
      controller.previous();
      return true;
    }, KeyMap.ctrl('f'));

    reader.getKeyMaps().get(LineReader.MAIN).bind((Widget) () -> {
      controller.openInstance();
      return true;
    }, KeyMap.ctrl('i'));
  }

  public void print(final String text) {
    reader.printAbove(text);
  }

  public void observeReader() {
    while (true) {
      final String comment = reader.readLine("comment > ");

      if (comment.equals("q")) {
        Try.lambda(terminal::close);
        return;
      }
      controller.command(comment);
      terminal.flush();
    }
  }
}

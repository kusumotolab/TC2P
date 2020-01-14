package com.github.kusumotolab.tc2p.core.view;

import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Widget;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import com.github.kusumotolab.tc2p.core.controller.IViewerController;
import com.github.kusumotolab.tc2p.core.entities.MiningResult.UsefulState;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.Try;
import lombok.Setter;

public class InteractiveConsoleView implements View {

  private final Terminal terminal = Try.force(TerminalBuilder::terminal);
  private final LineReader reader;
  private boolean isStandby = false;
  @Setter private IViewerController<?, ?> controller;

  public InteractiveConsoleView() {
    this.reader = LineReaderBuilder.builder().terminal(terminal).completer(new StringsCompleter("delete")).build();
    final KeyMap<Binding> keyMap = reader.getKeyMaps().get(LineReader.MAIN);

    keyMap.bind((Widget) () -> {
      controller.previous();
      return true;
    }, KeyMap.ctrl('j'));

    keyMap.bind((Widget) () -> {
      controller.next();
      return true;
    }, KeyMap.ctrl('k'));

    keyMap.bind((Widget) () -> {
      controller.openInstance();
      return true;
    }, KeyMap.ctrl('i'));

    keyMap.bind((Widget) () -> {
      controller.updateState(UsefulState.USEFUL);
      return true;
    }, KeyMap.ctrl('u'));

    keyMap.bind((Widget) () -> {
      controller.updateState(UsefulState.NOT_USEFUL);
      return true;
    }, KeyMap.ctrl('v'));
  }

  public void clear() {
    terminal.puts(Capability.clear_screen);
  }

  public void print(final String text) {
    reader.printAbove(text);
  }

  public void close() {
    Try.lambda(terminal::close);
    isStandby = false;
    System.out.println("Bye!");
  }

  public void observeReader() {
    isStandby = true;
    while (isStandby) {
      final String comment = reader.readLine("comment > ");
      controller.command(comment.trim());
      terminal.flush();
    }
  }
}

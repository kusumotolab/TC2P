package com.github.kusumotolab.tc2p.core.presenter;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.view.ConsoleView;
import com.github.kusumotolab.tc2p.utils.Colors;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MiningEditPatternPresenter extends IMiningEditPatternPresenter<ConsoleView> {

  public MiningEditPatternPresenter(final ConsoleView view) {
    super(view);
  }

  @Override
  public void startFetchEditScript() {
    view.print("Start Fetch");
  }

  @Override
  public void endFetchEditScript(final List<EditScript> editScripts) {
    view.print("Edit Scripts: " + editScripts.size());
  }

  @Override
  public void endConstructingTrees(final Set<Node<ASTLabel>> nodes) {
    view.print("Mining Target Trees: " + nodes.size());
  }

  @Override
  public void endMiningPatterns(final Set<TreePattern<ASTLabel>> patterns) {
    view.print("The Results: " + patterns.size());
  }

  @Override
  public void time(final String name, final Duration duration) {
    view.print("The Time of " + name + ": " + duration.getSeconds() + "(s)");
  }

  @Override
  public void pattern(final TreePattern<ASTLabel> pattern) {
    StringBuilder text = new StringBuilder("Frequency: " + pattern.countPatten() + "\n");
    final Node<ASTLabel> rootNode = pattern.getRootNode();
    for (final Label<ASTLabel> label : rootNode.getLabels()) {
      final String indent = Strings.repeat(" ", 2 * label.getDepth());
      text.append(indent)
          .append(label.getDepth())
          .append(" (")
          .append(toString(label.getLabel()))
          .append(")\n");
    }

    int index = 1;
    for (final String treeId : pattern.getTreeIds()) {
      text.append(index).append(": ").append(treeId).append("\n");
      index += 1;
    }

    text.append("\n");
    view.print(text.toString());
  }

  private String toString(final ASTLabel label) {
    final List<Column> columns = Lists.newArrayList();
    final List<String> actions = label.getActions().stream()
        .map(ActionEnum::toStringWithColor)
        .collect(Collectors.toList());
    if (!actions.isEmpty()) {
      columns.add(new Column("actions", String.join(", ", actions)));
    }

    columns.add(new Column("type", Colors.cyan("'" + label.getType() + "'")));

    if (label.getValue() != null && !label.getValue().isEmpty()) {
      columns.add(new Column("value", Colors.purple("'" + label.getValue() + "'")));
    }

    if (label.getNewValue() != null && !label.getNewValue().isEmpty()) {
      columns.add(new Column("newValue", Colors.purple("'" + label.getNewValue() + "'")));
    }
    return columns.stream()
        .map(Column::toString)
        .collect(Collectors.joining(", "));
  }

  @RequiredArgsConstructor
  private static class Column {

    @Getter private final String name;
    @Getter private final String value;

    @Override
    public String toString() {
      return name + "={" + value + "}";
    }
  }
}

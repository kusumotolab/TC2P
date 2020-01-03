package com.github.kusumotolab.tc2p.core.presenter;

import java.util.List;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.view.ConsoleView;
import com.github.kusumotolab.tc2p.utils.Colors;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ViewPresenter extends IViewPresenter<ConsoleView> {

  public ViewPresenter(final ConsoleView view) {
    super(view);
  }

  @Override
  public void show(final MiningResult result) {
    view.print(result.getProjectName() + ": " + result.getFrequency());
    view.print("max_depth = " + result.getMaxDepth());
    view.print("max_size = " + result.getSize());
    final StringBuilder text = new StringBuilder("\n");
    for (final Label<ASTLabel> label : result.getRoot().getLabels()) {
      final String indent = Strings.repeat(" ", 2 * label.getDepth());
      text.append(indent)
          .append(label.getDepth())
          .append(" (")
          .append(toString(label.getLabel()))
          .append(")\n");
    }
    view.print(text.toString());
    for (final String url : result.getUrls()) {
      view.print(url);
    }
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

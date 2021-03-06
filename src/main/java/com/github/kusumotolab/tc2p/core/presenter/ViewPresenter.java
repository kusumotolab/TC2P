package com.github.kusumotolab.tc2p.core.presenter;

import java.util.List;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.MiningResult.UsefulState;
import com.github.kusumotolab.tc2p.core.entities.Tag;
import com.github.kusumotolab.tc2p.core.view.InteractiveConsoleView;
import com.github.kusumotolab.tc2p.utils.Colors;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ViewPresenter extends IViewPresenter<InteractiveConsoleView> {

  public ViewPresenter(final InteractiveConsoleView view) {
    super(view);
  }

  @Override
  public void show(final MiningResult result, final int index) {
    view.clear();
    view.print("index = " + index);
    view.print("id = " + result.getId());
    view.print("project = " + result.getProjectName());
    view.print("frequency = " + result.getFrequency());
    view.print("max_depth = " + result.getMaxDepth());
    view.print("max_size = " + result.getSize());
    if (!result.getTags().isEmpty()) {
      view.print("tags = " + result.getTags().stream().map(Tag::getName).collect(Collectors.joining(", ")));
    }

    if (result.getComment() != null) {
      view.print("comment = " + result.getComment());
    }
    if (!result.getUsefulState().equals(UsefulState.NONE)) {
      view.print("state = " + result.getUsefulState().toString());
    }

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
    view.print("");
  }

  @Override
  public void observeInput() {
    view.observeReader();
  }

  @Override
  public void finish() {
    view.close();
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

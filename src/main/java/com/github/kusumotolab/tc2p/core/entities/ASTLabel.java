package com.github.kusumotolab.tc2p.core.entities;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"actions", "value", "newValue", "type"})
public class ASTLabel {

  @Getter private final int id;
  @Getter private final int parentId;
  @Getter private final List<ActionEnum> actions;
  @Getter private final String value;
  @Getter private final String newValue;
  @Getter private final String type;

  public ASTLabel(final TreeNode node) {
    this.id = node.getId();
    final TreeNode parentNode = node.getParentNode();
    this.parentId = parentNode != null ? parentNode.getId() : -1;
    this.actions = node.getActions().stream().sorted().collect(Collectors.toList());
    this.value = node.getValue();
    this.newValue = node.getNewValue();
    this.type = node.getType();
  }
}

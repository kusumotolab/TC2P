package com.github.kusumotolab.tc2p.core.entities;

import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@ToString(of = {"actions", "type"})
public class ASTLabel {

  @Getter private final int id;
  @Getter private final int parentId;
  @Getter private final List<ActionEnum> actions;
  @Getter private final String value;
  @Getter private final String newValue;
  @Getter private final String type;
  @Setter private Comparator comparator = new Comparator();

  public ASTLabel(final TreeNode node) {
    this.id = node.getId();
    final TreeNode parentNode = node.getParentNode();
    this.parentId = parentNode != null ? parentNode.getId() : -1;
    this.actions = node.getActions();
    this.value = node.getValue();
    this.newValue = node.getNewValue();
    this.type = node.getType();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return comparator.isEqual(this, ((ASTLabel) o));
  }

  @Override
  public int hashCode() {
    return comparator.hash(this);
  }

  public static class Comparator {
    public boolean isEqual(final ASTLabel l1, final ASTLabel l2) {
      return l1.actions.equals(l2.actions)
          && l1.value.equals(l2.value)
          && l1.newValue.equals(l2.newValue)
          && l1.type.equals(l2.type);
    }

    public int hash(final ASTLabel label) {
      return Objects.hash(label.actions, label.value, label.newValue, label.type);
    }
  }
}

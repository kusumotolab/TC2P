package com.github.kusumotolab.tc2p.core.entities;

import java.util.List;
import java.util.function.Function;
import com.github.kusumotolab.tc2p.utils.compare.IEquivalent;
import com.google.common.collect.Lists;
import lombok.Getter;

public class ASTLabel {

  @Getter private final int id;
  @Getter private final int parentId;
  @Getter private final List<ActionEnum> actions;
  @Getter private final String value;
  @Getter private final String newValue;
  @Getter private final String type;
  @Getter private final Equivalent equivalent = new Equivalent();

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
    return equivalent.equal(this, ((ASTLabel) o));
  }

  @Override
  public int hashCode() {
    return equivalent.hashCode(this);
  }

  private static class Equivalent extends IEquivalent<ASTLabel> {

    private final List<Function<ASTLabel, ?>> functions = Lists.newArrayList(
        e -> e.actions,
        e -> e.value,
        e -> e.newValue,
        e -> e.type
    );

    @Override
    public List<Function<ASTLabel, ?>> getFunctions() {
      return functions;
    }
  }
}

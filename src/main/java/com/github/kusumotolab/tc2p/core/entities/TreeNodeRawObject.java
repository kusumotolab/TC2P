package com.github.kusumotolab.tc2p.core.entities;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteColumn;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TreeNodeRawObject extends SQLiteObject {

  @Getter
  @SQLiteColumn(type = Types.CHAR, primaryKey = true)
  private String key;

  @Getter
  @SQLiteColumn(type = Types.INTEGER)
  private int id;

  @Getter
  @SQLiteColumn(type = Types.INTEGER)
  private int pos;

  @Getter
  @SQLiteColumn(type = Types.INTEGER, name = "parent_node_id")
  private int parentNodeId;

  @Getter
  @SQLiteColumn(type = Types.CHAR, name = "action_name")
  private List<ActionEnum> actions;

  @Getter
  @SQLiteColumn(type = Types.CHAR)
  private String value;

  @Getter
  @SQLiteColumn(type = Types.CHAR, name = "new_value")
  private String newValue;

  @Getter
  @SQLiteColumn(type = Types.CHAR)
  private String type;

  public TreeNodeRawObject(final String key, final int id, final int pos, final TreeNode parentNode,
      final List<ActionEnum> actions, final String value, final String newValue,
      final String type) {
    this.key = key;
    this.id = id;
    this.pos = pos;
    this.parentNodeId = parentNode != null ? parentNode.getId() : -1;
    this.actions = actions;
    this.value = value;
    this.newValue = newValue;
    this.type = type;
  }

  public TreeNode asTreeNode(final Function<Integer, TreeNode> resolver) {
    if (pos == -1) {
      return TreeNode.createRoot(key, id, actions, value, newValue, type);
    }
    final TreeNode parentNode = resolver.apply(parentNodeId);
    return parentNode.addChild(key, id, pos, actions, value, newValue, type);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Object encodeField(final Object value, final Field field) {
    final String name = field.getName();
    if (!name.equals("actions")) {
      return super.encodeField(value, field);
    }
    return ((List<ActionEnum>) value).stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  protected Object decodeField(final Object value, final Field field) {
    final String name = field.getName();
    if (!name.equals("actions")) {
      return super.decodeField(value, field);
    }
    if (((String) value).isEmpty()) {
      return Lists.newArrayList();
    }
    return Arrays.stream(((String) value).split(","))
        .map(ActionEnum::valueOf)
        .collect(Collectors.toList());
  }
}

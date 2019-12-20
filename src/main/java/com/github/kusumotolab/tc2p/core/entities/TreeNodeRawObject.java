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
  @SQLiteColumn(type = Types.CHAR, name = "project_name", primaryKey = true)
  private String projectName;

  @Getter
  @SQLiteColumn(type = Types.CHAR, name = "src_commit", primaryKey = true)
  private String srcCommitId;

  @Getter
  @SQLiteColumn(type = Types.CHAR, name = "dst_commit_id", primaryKey = true)
  private String dstCommitId;

  @Getter
  @SQLiteColumn(type = Types.INTEGER, primaryKey = true)
  private int id;

  @Getter
  @SQLiteColumn(type = Types.INTEGER)
  private int pos;

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

  public TreeNodeRawObject(final String projectName, final String srcCommitId,
      final String dstCommitId, final int id,
      final int pos, final TreeNode parentNode,
      final List<ActionEnum> actions, final String value, final String newValue,
      final String type) {
    this.projectName = projectName;
    this.srcCommitId = srcCommitId;
    this.dstCommitId = dstCommitId;
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
      return TreeNode.createRoot(projectName, srcCommitId, dstCommitId, id, actions, value,
          newValue, type);
    }
    final TreeNode parentNode = resolver.apply(parentNodeId);
    return parentNode.addChild(id, pos, actions, value, newValue, type);
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

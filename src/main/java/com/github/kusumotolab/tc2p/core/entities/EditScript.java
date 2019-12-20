package com.github.kusumotolab.tc2p.core.entities;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteColumn;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EditScript extends SQLiteObject {

  @SQLiteColumn(type = Types.INTEGER, primaryKey = true, autoIncrement = true)
  private int id;

  @SQLiteColumn(type = Types.CHAR, name = "src_commit_id")
  private String srcCommitID;

  @SQLiteColumn(type = Types.CHAR, name = "src_commit_message")
  private String srcCommitMessage;

  @SQLiteColumn(type = Types.CHAR, name = "dst_commit_id")
  private String dstCommitID;

  @SQLiteColumn(type = Types.CHAR, name = "dst_commit_message")
  private String dstCommitMessage;

  @SQLiteColumn(type = Types.CHAR, name = "src_name")
  private String srcName;

  @SQLiteColumn(type = Types.CHAR, name = "dst_name")
  private String dstName;

  @SQLiteColumn(type = Types.CHAR, name = "project_name", indexIds = {"project_name_index"})
  private String projectName;

  @SQLiteColumn(type = Types.CHAR, name = "tree_node_ids")
  private List<Integer> treeNodeIds = Lists.newArrayList();

  private List<TreeNode> treeNodes = Lists.newArrayList();

  @Override
  protected Object encodeField(final Object value, final Field field) {
    if (field.getName().equals("treeNodeIds")) {
      final List<String> ids = treeNodeIds.stream()
          .map(Object::toString)
          .collect(Collectors.toList());
      return String.join("&", ids);
    }
    return super.encodeField(value, field);
  }

  @Override
  public Object decodeField(final Object value, final Field field) {
    if (field.getName().equals("treeNodeIds")) {
      if (value.equals("")) {
        return Lists.newArrayList();
      }
      return Stream.of(((String) value).split("&"))
          .map(Integer::parseInt)
          .collect(Collectors.toList());
    }
    return super.decodeField(value, field);
  }
}

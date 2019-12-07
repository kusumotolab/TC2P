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

  @SQLiteColumn(type = Types.CHAR, name = "tree_node_keys")
  private List<String> treeNodeKeys;

  private List<TreeNode> treeNodes = new ArrayList<>();

  @Override
  protected Object encodeField(final Object value, final Field field) {
    if (field.getName().equals("treeNodeKeys")) {
      final List<String> ids = treeNodeKeys.stream()
          .map(String::valueOf)
          .collect(Collectors.toList());
      return String.join(" & ", ids);
    }
    return super.encodeField(value, field);
  }

  @Override
  public Object decodeField(final Object value, final Field field) {
    if (field.getName().equals("treeNodeKeys")) {
      if (value.equals("")) {
        return Lists.newArrayList();
      }
      return Stream.of(((String) value).split(" & "))
          .collect(Collectors.toList());
    }
    return super.decodeField(value, field);
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public String getSrcCommitID() {
    return srcCommitID;
  }

  public void setSrcCommitID(final String srcCommitID) {
    this.srcCommitID = srcCommitID;
  }

  public String getSrcCommitMessage() {
    return srcCommitMessage;
  }

  public void setSrcCommitMessage(final String srcCommitMessage) {
    this.srcCommitMessage = srcCommitMessage;
  }

  public String getDstCommitID() {
    return dstCommitID;
  }

  public void setDstCommitID(final String dstCommitID) {
    this.dstCommitID = dstCommitID;
  }

  public String getDstCommitMessage() {
    return dstCommitMessage;
  }

  public void setDstCommitMessage(final String dstCommitMessage) {
    this.dstCommitMessage = dstCommitMessage;
  }

  public String getSrcName() {
    return srcName;
  }

  public void setSrcName(final String srcName) {
    this.srcName = srcName;
  }

  public String getDstName() {
    return dstName;
  }

  public void setDstName(final String dstName) {
    this.dstName = dstName;
  }

  public List<String> getTreeNodeKeys() {
    return treeNodeKeys;
  }

  public void settTreeNodeIds(final List<String> treeNodeKeys) {
    this.treeNodeKeys = treeNodeKeys;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(final String projectName) {
    this.projectName = projectName;
  }

  public List<TreeNode> getTreeNodes() {
    return treeNodes;
  }

  public void setTreeNodes(final List<TreeNode> treeNodes) {
    this.treeNodes = treeNodes;
  }
}

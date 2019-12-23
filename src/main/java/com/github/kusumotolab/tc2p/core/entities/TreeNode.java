package com.github.kusumotolab.tc2p.core.entities;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import lombok.Getter;

public class TreeNode {

  @Getter private final String projectName;
  @Getter private final String srcCommitId;
  @Getter private final String srcFilePath;
  @Getter private final String dstCommitId;
  @Getter private final String dstFilePath;
  @Getter private int id;
  @Getter private final int pos;
  @Getter private TreeNode parentNode;
  @Getter private final List<ActionEnum> actions;
  @Getter private final String value;
  @Getter private final String newValue;
  @Getter private final String type;
  @Getter private final List<TreeNode> children = Lists.newArrayList();
  private Boolean cache_hasAction;

  private TreeNode(final String projectName, final String srcCommitId, final String srcFilePath, final String dstCommitId,
      final String dstFilePath, final int id, final int pos,
      final TreeNode parentNode, final List<ActionEnum> actions, final String value, final String newValue, final String type) {
    this.projectName = projectName;
    this.srcCommitId = srcCommitId;
    this.srcFilePath = srcFilePath;
    this.dstCommitId = dstCommitId;
    this.dstFilePath = dstFilePath;
    this.id = id;
    this.pos = pos;
    this.parentNode = parentNode;
    this.actions = actions;
    this.value = value;
    this.newValue = newValue;
    this.type = type;
  }

  public static TreeNode createRoot(final String projectName, final String srcCommitId, final String srcFilePath, final String dstCommitId,
      final String dstFilePath, final int id, final List<ActionEnum> actions, final String value, final String newValue,
      final String type) {
    return new TreeNode(projectName, srcCommitId, srcFilePath, dstCommitId, dstFilePath, id, -1, null, actions, value,
        newValue, type);
  }

  public TreeNode addChild(final int id, final int pos, final List<ActionEnum> actions,
      final String value, final String newValue, final String type) {
    final TreeNode treeNode = new TreeNode(projectName, srcCommitId, srcFilePath, dstCommitId, dstFilePath, id, pos, this, actions, value,
        newValue, type);
    children.add(treeNode);
    children.sort(Comparator.comparingInt(e -> e.pos));
    return treeNode;
  }

  public TreeNodeRawObject asRaw() {
    return new TreeNodeRawObject(projectName, srcCommitId, dstCommitId, id, pos, parentNode,
        actions, value, newValue, type);
  }

  public List<TreeNode> getDescents() {
    final List<TreeNode> list = Lists.newArrayList();
    list.add(this);
    for (final TreeNode child : children) {
      list.addAll(child.getDescents());
    }
    return list;
  }

  public void fixId() {
    fixId(0);
  }

  private int fixId(int id) {
    this.id = id;
    int nextId = id + 1;
    for (final TreeNode child : children) {
      nextId = child.fixId(nextId);
    }
    return nextId;
  }

  public TreeNode compactAndGetNewRootNode() {
    return this.compactAndGetNewRootNode(false);
  }

  /*
   - 上に何もない -> childrenの圧縮はする
      - 子が一つ -> その子がroot
      - 子が複数 -> 自分がroot
   - 上がmov　-> 無条件で全て残して、自分がroot
   - 上がupd -> childrenの圧縮はして、自分がroot
   */
  private TreeNode compactAndGetNewRootNode(final boolean isInUpdate) {

    final boolean hasUpd = actions.contains(ActionEnum.UPD);
    final boolean hasMove =
        actions.contains(ActionEnum.SRC_MOV) || actions.contains(ActionEnum.DST_MOVE);

    if (hasMove) {
      return this;
    }

    final List<TreeNode> removedChildren = children.stream()
        .filter(e -> !e.hasAction())
        .collect(Collectors.toList());
    removedChildren.forEach(e -> e.parentNode = null);
    children.removeAll(removedChildren);

    if (isInUpdate) {
      for (final TreeNode child : children) {
        child.compactAndGetNewRootNode(true);
      }
      return this;
    }

    if (children.size() == 1) {
      final TreeNode child = children.get(0);
      return child.compactAndGetNewRootNode(hasUpd);
    }

    for (final TreeNode child : children) {
      child.compactAndGetNewRootNode(hasUpd);
    }

    return this;
  }

  private boolean hasAction() {
    if (cache_hasAction != null) {
      return cache_hasAction;
    }

    if (!actions.isEmpty()) {
      cache_hasAction = true;
      return true;
    }
    for (final TreeNode child : children) {
      final boolean childHasAction = child.hasAction();
      if (childHasAction) {
        cache_hasAction = true;
        return true;
      }
    }
    cache_hasAction = false;
    return false;
  }
}

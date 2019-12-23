package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Addition;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.github.kusumotolab.tc2p.core.usecase.interactor.TreeNodeAdaptor.Input;
import com.github.kusumotolab.tc2p.tools.gumtree.ActionStore;
import com.github.kusumotolab.tc2p.tools.gumtree.ITreeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;

public class TreeNodeAdaptor implements Interactor<Input, Optional<TreeNode>> {

  @Override
  public Optional<TreeNode> execute(final Input input) {
    if (input.actions.isEmpty()) {
      return Optional.empty();
    }

    final ActionStore actionStore = new ActionStore(input.actions);
    final Map<ITree, TreeNode> treeNodeMap = Maps.newHashMap();

    unlinkIllegalMapping(input.mappingStore);
    final TreeNode root = createRootNode(input, actionStore, treeNodeMap);
    constructSrcTreeNode(input, actionStore, treeNodeMap);
    updateTreeNodeMapForMatchedNode(treeNodeMap, input);
    addDstTreeNode(input, treeNodeMap, actionStore);
    root.fixId();

    return Optional.of(root);
  }

  private void unlinkIllegalMapping(final MappingStore mappingStore) {
    final List<Mapping> illegalMapping = Lists.newArrayList();
    for (final Mapping m : mappingStore.asSet()) {
      final ITree src = m.getFirst();
      final ITree dst = m.getSecond();
      final ITree _dst = mappingStore.getDst(src);
      final ITree _src = mappingStore.getSrc(dst);

      // タイプが違う，もしくは1:1対応になっていないものは外す
      if (src.getType() != dst.getType() || src != _src || dst != _dst) {
        illegalMapping.add(m);
      }
    }
    for (final Mapping m : illegalMapping) {
      mappingStore.unlink(m.getFirst(), m.getSecond());
    }
  }

  private TreeNode createRootNode(final Input input, final ActionStore actionStore,
      final Map<ITree, TreeNode> treeNodeMap) {
    final ITree srcRoot = input.srcTreeContext.getRoot();
    final int id = srcRoot.getId();
    final TreeNode root = TreeNode
        .createRoot(input.projectName, input.srcCommitId, input.srcFilePath, input.dstCommitId, input.dstFilePath, id,
            actionStore.getActions(srcRoot, true), srcRoot.getLabel(), null,
            ITreeUtil.getNodeType(srcRoot, input.srcTreeContext));
    treeNodeMap.put(srcRoot, root);
    return root;
  }

  private void constructSrcTreeNode(final Input input, final ActionStore actionStore,
      final Map<ITree, TreeNode> treeNodeMap) {
    for (final ITree tree : input.srcTreeContext.getRoot().getDescendants()) {
      final TreeNode parentNode = treeNodeMap.get(tree.getParent());
      final int id = tree.getId();
      final TreeNode treeNode = parentNode.addChild(id, ITreeUtil.getPosInParent(tree),
          actionStore.getActions(tree, true), tree.getLabel(), actionStore.getNewValue(tree),
          ITreeUtil.getNodeType(tree, input.srcTreeContext));
      treeNodeMap.put(tree, treeNode);
    }
  }

  private void updateTreeNodeMapForMatchedNode(final Map<ITree, TreeNode> treeNodeMap,
      final Input input) {
    final Map<ITree, TreeNode> updatedMap = treeNodeMap.entrySet().stream()
        .filter(entry -> input.mappingStore.getDst(entry.getKey()) != null)
        .collect(Collectors.toMap(
            entry -> input.mappingStore.getDst(entry.getKey()),
            Entry::getValue,
            (entry1, entry2) -> entry1
        ));
    treeNodeMap.putAll(updatedMap);
  }

  private void addDstTreeNode(final Input input, final Map<ITree, TreeNode> treeNodeMap,
      final ActionStore actionStore) {
    final List<Addition> additions = actionStore.getAdditionsSortedByDepth();
    for (final Addition addition : additions) {
      if (addition instanceof Insert) {
        final Insert insert = (Insert) addition;
        final ITree insertedNode = insert.getNode();
        final TreeNode parentNode = treeNodeMap.get(insertedNode.getParent());
        final TreeNode newNode = parentNode.addChild(insertedNode.getId(), insert.getPosition(),
            actionStore.getActions(insertedNode, false), insertedNode.getLabel(), null,
            ITreeUtil.getNodeType(insertedNode, input.dstTreeContext));
        treeNodeMap.put(insertedNode, newNode);
      } else if (addition instanceof Move) {
        final Move move = (Move) addition;
        final ITree node = move.getNode();
        final ITree newParentNode = move.getParent();
        final TreeNode parentNode = treeNodeMap.get(newParentNode);
        if (parentNode == null) {
          throw new RuntimeException("Mapping Error");
        }
        final TreeNode treeNode = parentNode.addChild(node.getId(), move.getPosition(),
            actionStore.getActions(node, false), node.getLabel(), null,
            ITreeUtil.getNodeType(node, input.dstTreeContext));
        treeNodeMap.put(node, treeNode);
        addMovedChildren(node, input, actionStore, treeNodeMap);
      }
    }
  }

  private void addMovedChildren(final ITree movedNode, final Input input,
      final ActionStore actionStore, final Map<ITree, TreeNode> treeNodeMap) {
    for (final ITree child : movedNode.getChildren()) {
      if (actionStore.getDeleteMap().containsKey(child)
          || actionStore.getSrcMoveMap().containsKey(child)) {
        continue;
      }

      final ITree parent = child.getParent();
      final TreeNode parentTreeNode = treeNodeMap.get(parent);
      if (parentTreeNode == null) {
        throw new RuntimeException("Mapping Error");
      }
      final TreeNode treeNode = parentTreeNode.addChild(-child.getId(),
          ITreeUtil.getPosInParent(child), actionStore.getActions(child, false), child.getLabel(),
          null, ITreeUtil.getNodeType(child, input.srcTreeContext));
      treeNodeMap.put(child, treeNode);
      addMovedChildren(child, input, actionStore, treeNodeMap);
    }
  }

  @RequiredArgsConstructor
  public static class Input {

    private final String projectName;
    private final String srcCommitId;
    private final String srcFilePath;
    private final String dstCommitId;
    private final String dstFilePath;
    private final MappingStore mappingStore;
    private final TreeContext srcTreeContext;
    private final TreeContext dstTreeContext;
    private final List<Action> actions;
  }
}

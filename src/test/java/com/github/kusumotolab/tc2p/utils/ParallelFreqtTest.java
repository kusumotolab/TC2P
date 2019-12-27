package com.github.kusumotolab.tc2p.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.TreeNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ParallelFreqtTest {

  private Set<Node<ASTLabel>> roots;

  @Before
  public void setUp() throws Exception {
    roots = Sets.newHashSet();
    final TreeNode root_3nodes = TreeNode
        .createRoot("test", "xxxx", "src.java", "yyyy", "dst.java", 0, Lists.newArrayList(ActionEnum.DEL), "append", "",
            "MethodInvocation");
    root_3nodes.addChild(1, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation")
        .addChild(2, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation")
        .addChild(3, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation")
        .addChild(3, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation")
        .addChild(3, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation")
        .addChild(3, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation")
        .addChild(3, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation");
    root_3nodes.fixId();

    final TreeNode root_2nodes = TreeNode
        .createRoot("test", "aaaa", "src.java", "bbbb", "dst.java", 0, Lists.newArrayList(ActionEnum.DEL), "append", "",
            "MethodInvocation");
    root_2nodes.addChild(1, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation")
        .addChild(2, 0, Lists.newArrayList(ActionEnum.DEL), "append", "", "MethodInvocation");

    this.roots = Lists.newArrayList(root_3nodes, root_2nodes).stream()
        .map(this::convertToNode)
        .collect(Collectors.toSet());
  }


  private Node<ASTLabel> convertToNode(final TreeNode treeNode) {
    final Node<ASTLabel> rootNode = Node.createRootNode(new ASTLabel(treeNode));
    final Map<Integer, Node<ASTLabel>> map = Maps.newHashMap();
    map.put(rootNode.getLabel().getId(), rootNode);

    final List<TreeNode> descents = treeNode.getDescents();
    for (int i = 1; i < descents.size(); i++) {
      final TreeNode node = descents.get(i);
      final ASTLabel label = new ASTLabel(node);
      final Node<ASTLabel> parentNode = map.get(label.getParentId());

      final Node<ASTLabel> childNode = parentNode.createChildNode(label);
      map.put(childNode.getLabel().getId(), childNode);
    }
    return rootNode;
  }

  @Test
  public void testMining() {
    final ParallelFreqt parallelFreqt = new ParallelFreqt();
    final Set<TreePattern<ASTLabel>> results = parallelFreqt.mining(roots, 0.4);
    results.forEach(pattern -> {
      System.out.println("------");
      System.out.println(pattern.getRootNode().toLongString());
    });
  }
}
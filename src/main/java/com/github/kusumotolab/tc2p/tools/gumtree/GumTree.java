package com.github.kusumotolab.tc2p.tools.gumtree;

import java.io.IOException;
import java.util.List;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.kusumotolab.tc2p.tools.gumtree.jdt.JdtTreeGenerator;

public class GumTree {

  private final GumTreeInput input;

  public GumTree(final GumTreeInput input) {
    this.input = input;
  }
  
  public GumTreeOutput exec() throws IOException {
    final TreeContext srcTreeContext = getTreeContext(input.getSrcContents());
    final TreeContext dstTreeContext = getTreeContext(input.getDstContents());

    final ITree srcRoot = srcTreeContext.getRoot();
    final ITree dstRoot = dstTreeContext.getRoot();

    final Matcher matcher = Matchers.getInstance()
        .getMatcher(srcRoot, dstRoot);
    matcher.match();

    final ActionGenerator actionGenerator = new ActionGenerator(srcRoot, dstRoot,
        matcher.getMappings());
    final List<Action> actions = actionGenerator.generate();

    return new GumTreeOutput(input, srcTreeContext, dstTreeContext, matcher.getMappings(), actions);
  }

  public TreeContext getTreeContext(final String content) throws IOException {
    return new JdtTreeGenerator().generateFromString(content);
  }
}
package com.github.kusumotolab.tc2p.tools.gumtree;

import java.util.List;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.TreeContext;

public class GumTreeOutput {

  private final GumTreeInput input;
  private final TreeContext srcTreeContext;
  private final TreeContext dstTreeContext;
  private final MappingStore mappingStore;
  private final List<Action> actions;

  public GumTreeOutput(final GumTreeInput input, final TreeContext srcTreeContext,
      final TreeContext dstTreeContext, final MappingStore mappingStore,
      final List<Action> actions) {
    this.input = input;
    this.srcTreeContext = srcTreeContext;
    this.dstTreeContext = dstTreeContext;
    this.mappingStore = mappingStore;
    this.actions = actions;
  }

  public GumTreeInput getInput() {
    return input;
  }

  public TreeContext getSrcTreeContext() {
    return srcTreeContext;
  }

  public TreeContext getDstTreeContext() {
    return dstTreeContext;
  }

  public MappingStore getMappingStore() {
    return mappingStore;
  }

  public List<Action> getActions() {
    return actions;
  }
}

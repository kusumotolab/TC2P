package com.github.kusumotolab.tc2p.tools.gumtree;

import java.util.List;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.TreeContext;
import lombok.Data;

@Data
public class GumTreeOutput {
  private final GumTreeInput input;
  private final TreeContext srcTreeContext;
  private final TreeContext dstTreeContext;
  private final MappingStore mappingStore;
  private final List<Action> actions;
}

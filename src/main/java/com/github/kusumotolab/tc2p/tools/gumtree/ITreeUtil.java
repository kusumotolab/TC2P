package com.github.kusumotolab.tc2p.tools.gumtree;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

public class ITreeUtil {

  public static int getPosInParent(final ITree tree) {
    return tree.getParent().getChildPosition(tree);
  }

  public static String getNodeType(final ITree tree, final TreeContext context) {
    return context.getTypeLabel(tree.getType());
  }
}

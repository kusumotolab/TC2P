package com.github.kusumotolab.tc2p.core.entities;

import java.util.List;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MiningResult {
  private final String projectName;
  private final int frequency;
  private final int maxDepth;
  private final int size;
  private final Node<ASTLabel> root;
  private final List<String> urls;

  private String name;
  private String comment;
}

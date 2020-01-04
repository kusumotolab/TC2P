package com.github.kusumotolab.tc2p.core.entities;

import java.util.List;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class MiningResult {
  private final int id;
  private final String projectName;
  private final int frequency;
  private final int maxDepth;
  private final int size;
  private final Node<ASTLabel> root;
  private final List<String> urls;

  private String name;
  private String comment;
}

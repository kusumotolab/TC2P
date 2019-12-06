package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.nio.file.Path;
import java.util.Optional;
import com.github.kusumotolab.tc2p.core.entities.CommitLogPair;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTree;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeAdapter;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeInput;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeOutput;
import com.github.kusumotolab.tc2p.utils.Try;

public class GumTreeExecutor implements Interactor <CommitLogPair, Optional<GumTreeOutput>> {
  
  private final Path repositoryPath;

  public GumTreeExecutor(final Path repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  @Override
  public Optional<GumTreeOutput> execute(final CommitLogPair commitLogPair) {
    final GumTreeAdapter adapter = new GumTreeAdapter(repositoryPath);
    final Optional<GumTreeInput> inputOptional = adapter.convert(commitLogPair);
    return inputOptional.map(GumTree::new)
        .flatMap(gumTree -> Try.optional(gumTree::exec));
  }
}

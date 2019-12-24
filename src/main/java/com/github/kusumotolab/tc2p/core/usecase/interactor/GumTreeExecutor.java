package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.nio.file.Path;
import com.github.kusumotolab.tc2p.core.entities.CommitPair;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTree;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeAdapter;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeInput;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeOutput;
import io.reactivex.Observable;

public class GumTreeExecutor implements Interactor <CommitPair, Observable<GumTreeOutput>> {
  
  private final Path repositoryPath;

  public GumTreeExecutor(final Path repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  @Override
  public Observable<GumTreeOutput> execute(final CommitPair commitLogPair) {
    final GumTreeAdapter adapter = new GumTreeAdapter(repositoryPath);
    final Observable<GumTreeInput> inputs = adapter.convert(commitLogPair);
    return inputs.map(GumTree::new)
        .map(GumTree::exec);
  }
}

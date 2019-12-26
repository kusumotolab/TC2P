package com.github.kusumotolab.tc2p.core.presenter;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.TreePattern;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;

public abstract class IMiningEditPatternPresenter<V extends View> extends Presenter<V> {

  public IMiningEditPatternPresenter(final V view) {
    super(view);
  }

  public abstract void startFetchEditScript();

  public abstract void endFetchEditScript(final List<EditScript> editScripts);

  public abstract void endConstructingTrees(final Set<Node<ASTLabel>> nodes);

  public abstract void endMiningPatterns(final Set<TreePattern<ASTLabel>> patterns);

  public abstract void time(final String name, final Duration duration);

  public abstract void pattern(final TreePattern<ASTLabel> pattern);
}

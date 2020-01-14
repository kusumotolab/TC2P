package com.github.kusumotolab.tc2p.core.usecase;

import com.github.gumtreediff.actions.model.Update;
import com.github.kusumotolab.tc2p.core.configuration.ViewerConfiguration;
import com.github.kusumotolab.tc2p.core.entities.MiningResult.UsefulState;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;

public abstract class IViewUseCase <V extends View, P extends Presenter<V>> extends UseCase<ViewerConfiguration, V, P> {

  public IViewUseCase(final P presenter) {
    super(presenter);
  }

  public abstract void next();

  public abstract void previous();

  public abstract void open();

  public abstract void delete();

  public abstract void finish();

  public abstract void updateState(final UsefulState state);

  public abstract void addComment(final String comment);
}

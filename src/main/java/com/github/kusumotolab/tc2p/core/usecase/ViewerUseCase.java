package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import com.github.kusumotolab.tc2p.core.configuration.ViewerConfiguration;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.presenter.IViewPresenter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningEditPatternResultParser;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningEditPatternResultParser.Input;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.Try;

public class ViewerUseCase<V extends View, P extends IViewPresenter<V>> extends IViewUseCase<V, P> {

  public ViewerUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final ViewerConfiguration viewerConfiguration) {
    final List<String> allLines = Try.force(() -> Files.readAllLines(viewerConfiguration.getInputFilePath()));
    final Input input = new Input(allLines);
    final List<MiningResult> miningResults = new MiningEditPatternResultParser().execute(input);
    miningResults.sort(Comparator.comparingInt(MiningResult::getSize));
    for (final MiningResult miningResult : miningResults) {
      presenter.show(miningResult);
    }
  }
}

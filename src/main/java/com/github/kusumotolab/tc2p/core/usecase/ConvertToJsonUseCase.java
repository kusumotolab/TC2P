package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.file.Files;
import java.util.List;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningEditPatternResultParser;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.gson.GsonFactory;
import com.github.kusumotolab.tc2p.utils.FileUtil;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.gson.Gson;

public class ConvertToJsonUseCase<V extends View, P extends Presenter<V>, U extends IConvertToJsonUseCase<V, P>> extends IConvertToJsonUseCase<V, P> {

  public ConvertToJsonUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final List<String> allLines = Try.force(() -> Files.readAllLines(input.getInputPath()));
    final MiningEditPatternResultParser.Input parserInput = new MiningEditPatternResultParser.Input(allLines);
    final List<MiningResult> miningResults = new MiningEditPatternResultParser().execute(parserInput);

    final Gson gson = GsonFactory.create();

    final String json = gson.toJson(miningResults);
    FileUtil.overWrite(input.getOutputPath(), json);
  }
}

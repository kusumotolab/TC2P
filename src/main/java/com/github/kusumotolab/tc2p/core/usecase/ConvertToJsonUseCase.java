package com.github.kusumotolab.tc2p.core.usecase;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.gson.MiningResultAdapter;
import com.github.kusumotolab.tc2p.core.entities.gson.NodeAdapter;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningEditPatternResultParser;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConvertToJsonUseCase<V extends View, P extends Presenter<V>, U extends IConvertToJsonUseCase<V, P>> extends IConvertToJsonUseCase<V, P> {

  public ConvertToJsonUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final List<String> allLines = Try.force(() -> Files.readAllLines(input.getInputPath()));
    final MiningEditPatternResultParser.Input parserInput = new MiningEditPatternResultParser.Input(allLines);
    final List<MiningResult> miningResults = new MiningEditPatternResultParser().execute(parserInput);

    final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Node.class, new NodeAdapter())
        .registerTypeAdapter(MiningResult.class, new MiningResultAdapter())
        .serializeNulls()
        .create();

    final String json = gson.toJson(miningResults);
    Try.lambda(() -> {
      Files.createFile(input.getOutputPath());
      Files.write(input.getOutputPath(), Lists.newArrayList(json), Charset.defaultCharset(), StandardOpenOption.TRUNCATE_EXISTING);
    });
  }
}

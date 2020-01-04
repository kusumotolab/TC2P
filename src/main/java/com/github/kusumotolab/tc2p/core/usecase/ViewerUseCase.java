package com.github.kusumotolab.tc2p.core.usecase;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.sdl4j.util.CommandLine;
import com.github.kusumotolab.tc2p.core.configuration.ViewerConfiguration;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.gson.MiningResultAdapter;
import com.github.kusumotolab.tc2p.core.entities.gson.NodeAdapter;
import com.github.kusumotolab.tc2p.core.presenter.IViewPresenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ViewerUseCase<V extends View, P extends IViewPresenter<V>> extends IViewUseCase<V, P> {

  private List<MiningResult> miningResults = Lists.newArrayList();
  private int index = 0;

  public ViewerUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final ViewerConfiguration viewerConfiguration) {
    final String json = Try.force(() -> Files.readString(viewerConfiguration.getInputFilePath()));

    final Gson gson = new GsonBuilder().registerTypeAdapter(Node.class, new NodeAdapter())
        .registerTypeAdapter(MiningResult.class, new MiningResultAdapter())
        .serializeNulls()
        .create();
    final Type collectionType = new TypeToken<List<MiningResult>>(){}.getType();
    final List<MiningResult> miningResults = gson.fromJson(json, collectionType);

    miningResults.sort(Comparator.comparingInt(MiningResult::getSize).reversed());

    if (miningResults.isEmpty()) {
      return;
    }

    this.miningResults = miningResults;
    index = 0;
    presenter.show(miningResults.get(0));
    presenter.observeInput();
  }

  @Override
  public void next() {
    if (miningResults.isEmpty()) {
      return;
    }
    if (miningResults.size() == index + 1) {
      return;
    }
    index += 1;
    presenter.show(miningResults.get(index));
  }

  @Override
  public void previous() {
    if (miningResults.isEmpty()) {
      return;
    }
    if (index == 0) {
      return;
    }
    index -= 1;
    presenter.show(miningResults.get(index));

  }

  @Override
  public void open() {
    final List<String> urls = miningResults.get(index).getUrls();
    urls.stream()
        .distinct()
        .forEach(url -> {
          final CommandLine commandLine = new CommandLine();
          Try.force(() -> commandLine.execute("open", url));
        });
  }

  @Override
  public void addComment(final String comment) {
    System.out.println("Command : " + comment);
  }
}

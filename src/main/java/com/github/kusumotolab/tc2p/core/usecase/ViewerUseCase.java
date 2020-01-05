package com.github.kusumotolab.tc2p.core.usecase;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.util.CommandLine;
import com.github.kusumotolab.tc2p.core.configuration.ViewerConfiguration;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.presenter.IViewPresenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.gson.GsonFactory;
import com.github.kusumotolab.tc2p.utils.FileUtil;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ViewerUseCase<V extends View, P extends IViewPresenter<V>> extends IViewUseCase<V, P> {

  private List<MiningResult> miningResults = Lists.newArrayList();
  private int index = 0;
  private Path jsonPath;

  public ViewerUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final ViewerConfiguration viewerConfiguration) {
    this.jsonPath = viewerConfiguration.getInputFilePath();
    final String json = Try.force(() -> Files.readString(viewerConfiguration.getInputFilePath()));
    final Gson gson = GsonFactory.create();

    final Type collectionType = new TypeToken<List<MiningResult>>(){}.getType();
    final List<MiningResult> miningResults = gson.fromJson(json, collectionType);

    final List<MiningResult> tmpMiningResults = miningResults.stream()
        .filter(e -> !e.isDeleted())
//        .sorted(Comparator.comparingInt(MiningResult::getSize).reversed())
        .collect(Collectors.toList());

    if (tmpMiningResults.isEmpty()) {
      return;
    }

    this.miningResults = tmpMiningResults;
    index = 0;
    presenter.show(this.miningResults.get(0), index);
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
    presenter.show(miningResults.get(index), index);
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
    presenter.show(miningResults.get(index), index);

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
  public void delete() {
    miningResults.get(index).setDeleted(true);
    miningResults.remove(index);
    if (miningResults.isEmpty()) {
      finish();
      return;
    }
    final MiningResult miningResult = miningResults.get(index);
    presenter.show(miningResult, index);
  }

  @Override
  public void finish() {
    miningResults.sort(Comparator.comparingInt(MiningResult::getId));
    final Gson gson = GsonFactory.create();

    final String json = gson.toJson(miningResults);
    FileUtil.overWrite(jsonPath, json);
    presenter.finish();
  }

  @Override
  public void addComment(final String comment) {
    miningResults.get(index).setComment(comment);
    next();
  }
}

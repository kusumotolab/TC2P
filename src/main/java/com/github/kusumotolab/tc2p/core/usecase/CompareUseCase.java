package com.github.kusumotolab.tc2p.core.usecase;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.BaseLabel;
import com.github.kusumotolab.tc2p.core.entities.BaseResult;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.PatternPosition;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.tools.db.Query;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLite;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteQuery;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import io.reactivex.Observable;

public class CompareUseCase<V extends View, P extends Presenter<V>> extends ICompareUseCase<V, P> {

  public CompareUseCase(final P presenter) {
    super(presenter);
  }

  @Override
  public void execute(final Input input) {
    final SQLite baseSQLite = new SQLite(input.getBaseSQLitePath());
    final SQLite tc2pSQLite = new SQLite(input.getTc2pSQLitePath());

    baseSQLite.connect()
        .andThen(tc2pSQLite.connect()).blockingAwait();


    final Observable<BaseResult> convertedMiningResults = tc2pSQLite.fetch(createQueryForMiningResult())
        .map(this::convertToBaseResult);

    final Map<Set<BaseLabel>, BaseResult> map = Maps.newHashMap();
    convertedMiningResults.subscribe(result -> {
      final BaseResult previousResult = map.get(result.getActions());
      if (previousResult == null) {
        map.put(result.getActions(), result);
        return;
      }
      final Set<PatternPosition> newPatternPositions = Sets.newHashSet();
      newPatternPositions.addAll(result.getPatternPositions());
      newPatternPositions.addAll(previousResult.getPatternPositions());
      map.put(result.getActions(),
          new BaseResult(result.getId(), result.getProjectName(), newPatternPositions.size(), result.getActions(), result.getActionSize(),
              Lists.newArrayList(newPatternPositions)));
    });

    final Set<BaseResult> baseSet = Sets.newHashSet();
    final Set<BaseResult> miningSet = Sets.newHashSet(map.values());

    final Observable<BaseResult> baseResults = baseSQLite.fetch(createQueryForBaseResult());
    baseResults.subscribe(baseSet::add);

    final SetView<BaseResult> intersection = Sets.intersection(baseSet, miningSet);
    final SetView<BaseResult> baseOnly = Sets.difference(baseSet, intersection);
    final SetView<BaseResult> tc2pOnly = Sets.difference(miningSet, intersection);

    presenter.show("The Base: " + baseOnly.size());
    presenter.show("Intersection: " + intersection.size());
    presenter.show("TC2P: " + tc2pOnly.size());

    final long countPatternWhichContainsMove = tc2pOnly.parallelStream()
        .filter(baseResult -> baseResult.getActions().parallelStream()
            .map(BaseLabel::getAction)
            .anyMatch(e -> e.equals(ActionEnum.SRC_MOV) || e.equals(ActionEnum.DST_MOVE)))
        .count();
    presenter.show("TC2P(Move): " + countPatternWhichContainsMove);
  }

  private Query<BaseResult> createQueryForBaseResult() {
    return SQLiteQuery.select(BaseResult.class)
        .from(BaseResult.class)
        .build();
  }

  private Query<MiningResult> createQueryForMiningResult() {
    return SQLiteQuery.select(MiningResult.class)
        .from(MiningResult.class)
        .orderBy("action_size", false)
        .build();
  }

  private BaseResult convertToBaseResult(final MiningResult miningResult) {
    final Set<BaseLabel> baseLabels = miningResult.getRoot().getLabels().stream()
        .map(Label::getLabel)
        .flatMap(label -> label.getActions().stream()
            .map(action -> new BaseLabel(label.getId(), action, label.getType())))
        .filter(RxBaseUseCase::filter)
        .collect(Collectors.toSet());
    return new BaseResult(miningResult.getId(), miningResult.getProjectName(), miningResult.getFrequency(), baseLabels, baseLabels.size(),
        miningResult.getPatternPositions());
  }

  private Set<BaseResult> completeSubset(final Set<BaseResult> results) {
    final Set<BaseResult> addSet = Sets.newHashSet();
    final Set<Set<BaseLabel>> check = Sets.newHashSet();

    for (final BaseResult result : results) {
      final Set<BaseLabel> actions = result.getActions();
      final Set<Set<BaseLabel>> set = Sets.newHashSet();
      recursiveSearch(actions, set, check);
      set.parallelStream()
          .map(baseLabels -> new BaseResult(result.getId(), result.getProjectName(), result.getFrequency(), baseLabels, baseLabels.size(),
              result.getPatternPositions()))
          .sequential()
          .forEach(addSet::add);
    }

    return addSet;
  }

  private void recursiveSearch(final Set<BaseLabel> labels, final Set<Set<BaseLabel>> results, final Set<Set<BaseLabel>> checks) {
    if (checks.contains(labels)) {
      return;
    }

    results.add(labels);
    checks.add(labels);

    for (final BaseLabel label : labels) {
      final Set<BaseLabel> set = Sets.newHashSet(labels);
      set.remove(label);
      recursiveSearch(set, results, checks);
    }
  }
}

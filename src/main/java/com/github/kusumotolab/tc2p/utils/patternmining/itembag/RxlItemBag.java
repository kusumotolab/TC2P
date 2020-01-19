package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class RxlItemBag<Item> {

  private final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//
//  private final ExecutorService service = Executors.newFixedThreadPool(1);

  public Observable<ITNode<Item>> mining(final Set<Transaction<Item>> transactions, final int minimumSupport, final int dt) {
    return Observable.create(emitter -> {
      final List<ITNode<Item>> f1 = extractF1(transactions, dt);

      final List<Future<?>> futures = Lists.newArrayList();
      for (int i = 0; i < f1.size() - 1; i++) {
        final ITNode<Item> node = f1.get(i);
        emitter.onNext(node);
        for (int j = i + 1; j < f1.size(); j++) {
          final int finalJ = j;
          final Future<?> future = service.submit(() -> recursiveMining(emitter, node, finalJ, f1, minimumSupport, dt));
          futures.add(future);
        }
      }
      futures.forEach(e -> Try.lambda(() -> e.get(10, TimeUnit.DAYS)));
      emitter.onComplete();
    });
  }

  public Completable shutdown() {
    return Completable.create(emitter -> {
      service.shutdown();
      Try.lambda(() -> service.awaitTermination(10, TimeUnit.DAYS));
      emitter.onComplete();
    });
  }

  private List<ITNode<Item>> extractF1(final Set<Transaction<Item>> transactions, final int dt) {
    final Multimap<Item, TransactionID> itemTransactionIdMap = HashMultimap.create();
    final Map<TransactionID, Map<Item, Occurrences>> itemAndTransactionIdToOccurrenceIdMap = new HashMap<>();

    for (final Transaction<Item> transaction : transactions) {
      for (final ItemAndOccurrence<Item> itemAndOccurrence : transaction.getItemAndOccurrences()) {
        itemTransactionIdMap.put(itemAndOccurrence.getItem(), transaction.getId());
        final Map<Item, Occurrences> itemToOccurrenceMap = itemAndTransactionIdToOccurrenceIdMap
            .getOrDefault(transaction.getId(), Maps.newHashMap());
        final Occurrences occurrences = itemToOccurrenceMap.getOrDefault(itemAndOccurrence.getItem(), new Occurrences());
        occurrences.getOccurrences().add(itemAndOccurrence.getOccurrence());

        itemToOccurrenceMap.put(itemAndOccurrence.getItem(), occurrences);
        itemAndTransactionIdToOccurrenceIdMap.put(transaction.getId(), itemToOccurrenceMap);
      }
    }

    final List<ITNode<Item>> results = Lists.newArrayList();

    for (final Item key : itemTransactionIdMap.keySet()) {
      final Set<TransactionID> transactionIds = Sets.newHashSet(itemTransactionIdMap.get(key));
      final int size = transactionIds.size();
      final Threshold threshold = Threshold.classify(size);
      if (size < threshold.aft || size < threshold.dt) {
        continue;
      }
      final Map<TransactionID, Map<Item, Occurrences>> transactionIdItemToOccurrenceMap = Maps.newHashMap();
      for (final TransactionID transactionId : transactionIds) {
        final Map<Item, Occurrences> itemOccurrencesMap = itemAndTransactionIdToOccurrenceIdMap.get(transactionId);
        transactionIdItemToOccurrenceMap.put(transactionId, itemOccurrencesMap);
      }

      final Set<Item> itemSet = Sets.newHashSet();
      itemSet.add(key);
      final ITNode<Item> node = new ITNode<>(itemSet, transactionIds, transactionIdItemToOccurrenceMap);
      results.add(node);
    }

    return results;
  }

  private void recursiveMining(final ObservableEmitter<ITNode<Item>> emitter, final ITNode<Item> subtree, final int index,
      final List<ITNode<Item>> f1, final int minimumSupport, final int dt) {
    final ITNode<Item> addNode = f1.get(index);

    final Set<TransactionID> newTransactionIds = Sets.intersection(subtree.getTransactionIds(), addNode.getTransactionIds());

    final Threshold threshold = Threshold.classify(addNode);

    if (newTransactionIds.size() < threshold.aft) {
      return;
    }

    if (newTransactionIds.size() * (subtree.getItemSet().size() + 1) < threshold.dt) {
      return;
    }

    final Set<Item> itemSet = subtree.getItemSet();
    final SetView<Item> newItemSet = Sets.union(itemSet, addNode.getItemSet());
    final Map<TransactionID, Map<Item, Occurrences>> newTransactionIDOccurrencesMap = Maps.newHashMap();
    newTransactionIDOccurrencesMap.putAll(subtree.getTransactionIDOccurrencesMap());
    for (final Entry<TransactionID, Map<Item, Occurrences>> entry : addNode.getTransactionIDOccurrencesMap().entrySet()) {
      final Map<Item, Occurrences> occurrencesMap = newTransactionIDOccurrencesMap.getOrDefault(entry.getKey(), Maps.newHashMap());
      occurrencesMap.putAll(entry.getValue());
      newTransactionIDOccurrencesMap.put(entry.getKey(), occurrencesMap);
    }
    newTransactionIDOccurrencesMap.putAll(addNode.getTransactionIDOccurrencesMap());
    final ITNode<Item> newNode = new ITNode<>(newItemSet, newTransactionIds, newTransactionIDOccurrencesMap);

    emitter.onNext(newNode);
    for (int i = index + 1; i < f1.size(); i++) {
      recursiveMining(emitter, newNode, i, f1, minimumSupport, dt);
    }
  }

  public enum Threshold {
    LARGE(500, 10), NORMAL(100, 10), SMALL(10, 10), IGNORE(Integer.MAX_VALUE, Integer.MAX_VALUE);

    final int dt;
    final int aft;

    Threshold(final int dt, final int aft) {
      this.dt = dt;
      this.aft = aft;
    }

    public static <T> Threshold classify(final ITNode<T> node) {
      return classify(node.getTransactionIds().size());
    }

    public static Threshold classify(final int size) {
      if (10 <= size && size < NORMAL.dt) {
        return SMALL;
      } else if (NORMAL.dt <= size && size < LARGE.dt) {
        return NORMAL;
      } else if (LARGE.dt <= size) {
        return LARGE;
      }
      return IGNORE;
    }
  }
}

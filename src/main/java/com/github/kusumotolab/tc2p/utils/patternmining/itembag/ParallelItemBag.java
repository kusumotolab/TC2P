package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import java.util.Collection;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParallelItemBag<Item> {

  private final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public ITNode<Item> mining(final Set<Transaction<Item>> transactions, final int minimumSupport) {
    final ITNode<Item> root = ITNode.createRoot();
    final List<ITNode<Item>> f1 = extractF1(root, transactions, minimumSupport);

    final List<Future<?>> futures = Lists.newArrayList();
    for (int i = 0; i < f1.size() - 1; i++) {
      final ITNode<Item> node = f1.get(i);
      for (int j = i + 1; j < f1.size(); j++) {
        final int finalJ = j;
        final Future<?> future = service.submit(() -> recursiveMining(node, finalJ, f1, minimumSupport));
        futures.add(future);
      }
    }
    futures.forEach(e -> Try.lambda(() -> e.get(10, TimeUnit.DAYS)));
    return root;
  }

  public void shutdown() {
    service.shutdown();
    Try.lambda(() -> service.awaitTermination(10, TimeUnit.DAYS));
  }

  private List<ITNode<Item>> extractF1(final ITNode<Item> root, final Set<Transaction<Item>> transactions, final int minimumSupport) {
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

    final Set<Item> removedKeys = Sets.newHashSet();
    itemTransactionIdMap.keySet().parallelStream()
        .forEach(key -> {
          final Collection<TransactionID> transactionIds = itemTransactionIdMap.get(key);
          if (transactionIds.size() < minimumSupport) {
            removedKeys.add(key);
          }
        });
    for (final Item key : removedKeys) {
      itemTransactionIdMap.removeAll(key);
    }

    final List<ITNode<Item>> results = Lists.newArrayList();

    for (final Item key : itemTransactionIdMap.keySet()) {
      final Set<TransactionID> transactionIds = Sets.newHashSet(itemTransactionIdMap.get(key));
      final Map<TransactionID, Map<Item, Occurrences>> transactionIdItemToOccurrenceMap = Maps.newHashMap();
      for (final TransactionID transactionId : transactionIds) {
        final Map<Item, Occurrences> itemOccurrencesMap = itemAndTransactionIdToOccurrenceIdMap.get(transactionId);
        transactionIdItemToOccurrenceMap.put(transactionId, itemOccurrencesMap);
      }

      final Set<Item> itemSet = Sets.newHashSet();
      itemSet.add(key);
      final ITNode<Item> node = new ITNode<>(itemSet, transactionIds, transactionIdItemToOccurrenceMap);
      root.getChildren().add(node);
      results.add(node);
    }

    return results;
  }

  private void recursiveMining(final ITNode<Item> subtree, final int index, final List<ITNode<Item>> f1, final int minimumSupport) {
    final ITNode<Item> addNode = f1.get(index);

    final Set<TransactionID> newTransactionIds = Sets.intersection(subtree.getTransactionIds(), addNode.getTransactionIds());
    if (newTransactionIds.size() < minimumSupport) {
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
    subtree.getChildren().add(newNode);

    for (int i = index + 1; i < f1.size(); i++) {
      recursiveMining(newNode, i, f1, minimumSupport);
    }
  }
}

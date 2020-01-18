package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;
import io.reactivex.Observable;

public class RxlItemBagTest {

  private final Transaction<String> transaction1 = new Transaction<>(new TransactionID("1"), Lists.newArrayList(
      new ItemAndOccurrence<>("a", new Occurrence("1")),
      new ItemAndOccurrence<>("b", new Occurrence("2")),
      new ItemAndOccurrence<>("c", new Occurrence("3")),
      new ItemAndOccurrence<>("c", new Occurrence("4")),
      new ItemAndOccurrence<>("c", new Occurrence("5")),
      new ItemAndOccurrence<>("d", new Occurrence("6")),
      new ItemAndOccurrence<>("e", new Occurrence("7")),
      new ItemAndOccurrence<>("h", new Occurrence("8"))
  ));
  private final Transaction<String> transaction2 = new Transaction<>(new TransactionID("2"), Lists.newArrayList(
      new ItemAndOccurrence<>("g", new Occurrence("9")),
      new ItemAndOccurrence<>("c", new Occurrence("10")),
      new ItemAndOccurrence<>("c", new Occurrence("11")),
      new ItemAndOccurrence<>("c", new Occurrence("12")),
      new ItemAndOccurrence<>("d", new Occurrence("13")),
      new ItemAndOccurrence<>("e", new Occurrence("14")),
      new ItemAndOccurrence<>("f", new Occurrence("15")),
      new ItemAndOccurrence<>("g", new Occurrence("16"))
  ));

  private final Transaction<String> transaction3 = new Transaction<>(new TransactionID("3"), Lists.newArrayList(
      new ItemAndOccurrence<>("c", new Occurrence("17")),
      new ItemAndOccurrence<>("d", new Occurrence("18")),
      new ItemAndOccurrence<>("h", new Occurrence("19"))
  ));

  @Test
  public void testMining() {
    final Set<Transaction<String>> transactions = Sets.newHashSet(Lists.newArrayList(transaction1, transaction2, transaction3));
    final RxlItemBag<String> itemBag = new RxlItemBag<>();
    final List<ITNode<String>> nodes = itemBag.mining(transactions, 2).toList().blockingGet();
    assertThat(nodes).hasSize(2);

    for (final ITNode<String> node : nodes) {
      final String text = node.getItemSet().stream()
          .sorted()
          .collect(Collectors.joining(", "));
      System.out.println(text);
    }
  }
}
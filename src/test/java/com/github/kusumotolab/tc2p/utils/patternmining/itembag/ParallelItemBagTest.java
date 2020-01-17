package com.github.kusumotolab.tc2p.utils.patternmining.itembag;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;

public class ParallelItemBagTest {

  private final Transaction<String> transaction1 = new Transaction<>(new TransactionID("1"), Lists.newArrayList(
      new ItemAndOccurrence<>("a", new Occurrence("1")),
      new ItemAndOccurrence<>("b", new Occurrence("2")),
      new ItemAndOccurrence<>("c", new Occurrence("3")),
      new ItemAndOccurrence<>("c", new Occurrence("4")),
      new ItemAndOccurrence<>("c", new Occurrence("5")),
      new ItemAndOccurrence<>("d", new Occurrence("6")),
      new ItemAndOccurrence<>("e", new Occurrence("7")),
      new ItemAndOccurrence<>("f", new Occurrence("8"))
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


  @Test
  public void testMining() {
    final Set<Transaction<String>> transactions = Sets.newHashSet(Lists.newArrayList(transaction1, transaction2));
    final ParallelItemBag<String> itemBag = new ParallelItemBag<>();
    final ITNode<String> root = itemBag.mining(transactions, 2);
    itemBag.shutdown();
    assertThat(root.size()).isEqualTo(16);
    final List<ITNode<String>> nodes = flat(root);

    nodes.remove(0);
    for (final ITNode<String> node : nodes) {
      final String text = node.getItemSet().stream()
          .sorted()
          .collect(Collectors.joining(", "));
      System.out.println(text);
    }
  }

  private List<ITNode<String>> flat(final ITNode<String> node) {
    final ArrayList<ITNode<String>> results = Lists.newArrayList(node);
    for (final ITNode<String> child : node.getChildren()) {
      results.addAll(flat(child));
    }
    return results;
  }
}

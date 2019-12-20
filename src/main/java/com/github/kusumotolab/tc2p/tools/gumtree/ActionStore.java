package com.github.kusumotolab.tc2p.tools.gumtree;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Addition;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.google.common.collect.Lists;
import lombok.Getter;

public class ActionStore {

  private final List<Action> actions;
  @Getter private final Map<ITree, Insert> insertMap;
  @Getter private final Map<ITree, Delete> deleteMap;
  @Getter private final Map<ITree, Update> updateMap;
  @Getter private final Map<ITree, Move> srcMoveMap;

  public ActionStore(final List<Action> actions) {
    this.actions = actions;
    insertMap = actions.stream()
        .filter(e -> e instanceof Insert)
        .collect(Collectors.toMap(Action::getNode, e -> ((Insert) e)));
    deleteMap = actions.stream()
        .filter(e -> e instanceof Delete)
        .collect(Collectors.toMap(Action::getNode, e -> ((Delete) e)));
    updateMap = actions.stream()
        .filter(e -> e instanceof Update)
        .collect(Collectors.toMap(Action::getNode, e -> ((Update) e)));
    srcMoveMap = actions.stream()
        .filter(e -> e instanceof Move)
        .collect(Collectors.toMap(Action::getNode, e -> ((Move) e)));
  }


  public String getNewValue(final ITree tree) {
    final Update update = updateMap.get(tree);
    if (update == null) {
      return null;
    }
    return update.getValue();
  }

  public List<ActionEnum> getActions(final ITree tree, final boolean isSrc) {
    final List<ActionEnum> list = Lists.newArrayList();
    if (isSrc) {
      final Delete delete = deleteMap.get(tree);
      if (delete != null) {
        list.add(ActionEnum.DEL);
        return list;
      }

      final Move move = srcMoveMap.get(tree);
      if (move != null) {
        list.add(ActionEnum.SRC_MOV);
      }
      final Update update = updateMap.get(tree);
      if (update != null) {
        list.add(ActionEnum.UPD);
      }
    } else {
      final Insert insert = insertMap.get(tree);
      if (insert != null) {
        list.add(ActionEnum.INS);
        return list;
      }

      final Move move = srcMoveMap.get(tree);
      if (move != null) {
        list.add(ActionEnum.DST_MOVE);
      }
      final Update update = updateMap.get(tree);
      if (update != null) {
        list.add(ActionEnum.UPD);
      }
    }
    return list;
  }

  public List<Addition> getAdditionsSortedByDepth() {
    return actions.stream()
        .filter(action -> action instanceof Addition)
        .sorted(Comparator.comparingInt(action -> {
          if (action instanceof Insert) {
            return action.getNode().getDepth();
          } else if (action instanceof Move) {
            return ((Move) action).getParent().getDepth() + 1;
          }
          throw new RuntimeException("Sorted Error for Additions");
        }))
        .map(action -> ((Addition) action))
        .collect(Collectors.toList());
  }
}

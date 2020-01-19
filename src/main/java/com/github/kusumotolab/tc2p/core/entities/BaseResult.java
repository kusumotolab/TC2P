package com.github.kusumotolab.tc2p.core.entities;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteColumn;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import com.github.kusumotolab.tc2p.tools.gson.GsonFactory;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ITNode;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.TransactionID;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseResult extends SQLiteObject {

  @SQLiteColumn(type = Types.INTEGER, primaryKey = true, autoIncrement = true)
  private int id;

  @SQLiteColumn(type = Types.CHAR, name = "project_name")
  private String projectName;

  @SQLiteColumn(type = Types.INTEGER)
  private int frequency;

  @SQLiteColumn(type = Types.CHAR)
  private Set<BaseLabel> actions;

  @SQLiteColumn(type = Types.INTEGER, name = "action_size")
  private int actionSize;

  @SQLiteColumn(type = Types.CHAR, name = "positions")
  private List<PatternPosition> patternPositions;

  private static final Gson GSON = GsonFactory.create();


  public BaseResult(final String projectName, final ITNode<BaseLabel> node) {
    this.projectName = projectName;
    this.frequency = node.maximumFrequency();
    this.actions = node.getItemSet();
    this.actionSize = actions.size();
    this.patternPositions = node.getTransactionIds().stream()
        .map(TransactionID::getValue)
        .map(PatternPosition::parse)
        .collect(Collectors.toList());
  }

  @Override
  protected Object encodeField(final Object value, final Field field) {
    if (field.getName().equals("patternPositions")) {
      return GSON.toJson(value);
    }
    if (field.getName().equals("actions")) {
      return GSON.toJson(Lists.newArrayList(actions));
    }
    return super.encodeField(value, field);
  }

  @Override
  protected Object decodeField(final Object value, final Field field) {
    if (field.getName().equals("patternPositions")) {
      Type listType = new TypeToken<List<PatternPosition>>() {
      }.getType();
      return GSON.fromJson(((String) value), listType);
    }
    if (field.getName().equals("actions")) {

      Type setType = new TypeToken<List<BaseLabel>>() {
      }.getType();
      return Sets.newHashSet(((List<BaseLabel>) GSON.fromJson(((String) value), setType)));
    }
    return super.decodeField(value, field);
  }
}

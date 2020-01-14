package com.github.kusumotolab.tc2p.core.entities;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteColumn;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import com.github.kusumotolab.tc2p.tools.gson.GsonFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MiningResult extends SQLiteObject {

  @SQLiteColumn(type = Types.INTEGER, primaryKey = true)
  private int id;

  @SQLiteColumn(type = Types.CHAR, name = "project_name")
  private String projectName;

  @SQLiteColumn(type = Types.INTEGER)
  private int frequency;

  @SQLiteColumn(type = Types.INTEGER, name = "max_depth")
  private int maxDepth;

  @SQLiteColumn(type = Types.INTEGER)
  private int size;

  @SQLiteColumn(type = Types.INTEGER, name = "action_size")
  private int actionSize;

  @SQLiteColumn(type = Types.CHAR)
  private Node<ASTLabel> root;

  @SQLiteColumn(type = Types.CHAR)
  private List<String> urls;

  @SQLiteColumn(type = Types.CHAR)
  private List<Tag> tags;

  @SQLiteColumn(type = Types.BOOLEAN, name = "is_deleted")
  private boolean isDeleted = false;

  @SQLiteColumn(type = Types.CHAR)
  private String name;

  @SQLiteColumn(type = Types.CHAR)
  private String comment;

  @SQLiteColumn(type = Types.CHAR, name = "useful_state")
  private UsefulState usefulState = UsefulState.NONE;

  private static final Gson GSON = GsonFactory.create();

  public MiningResult(final int id, final String projectName, final int frequency, final int maxDepth, final int size,
      final Node<ASTLabel> root, final List<String> urls) {
    this.id = id;
    this.projectName = projectName;
    this.frequency = frequency;
    this.maxDepth = maxDepth;
    this.size = size;
    this.root = root;
    this.actionSize = root.getLabels().stream()
        .reduce(0, (result, label) -> result + label.getLabel().getActions().size(), Integer::sum);
    this.urls = urls;
    this.tags = convertToTags(root);
  }

  public MiningResult(final int id, final String projectName, final int frequency, final int maxDepth, final int size,
      final Node<ASTLabel> node, final List<String> urls, final boolean isDeleted, final String name, final String comment, final UsefulState usefulState) {
    this.id = id;
    this.projectName = projectName;
    this.frequency = frequency;
    this.maxDepth = maxDepth;
    this.size = size;
    this.actionSize =  root.getLabels().stream()
        .reduce(0, (result, label) -> result + label.getLabel().getActions().size(), Integer::sum);
    this.root = node;
    this.urls = urls;
    this.tags = convertToTags(root);
    this.isDeleted = isDeleted;
    this.name = name;
    this.comment = comment;
    this.usefulState = usefulState;
  }

  private List<Tag> convertToTags(final Node<ASTLabel> node) {
    return node.getLabels().stream()
        .map(Label::getLabel)
        .map(ASTLabel::getType)
        .map(Tag::create)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  protected Object encodeField(final Object value, final Field field) {
    if (field.getName().equals("root")) {
      return GSON.toJson(value);
    }
    if (field.getName().equals("urls")) {
      return String.join("===", urls);
    }
    if (field.getName().equals("tags")) {
      return tags.stream()
          .map(Enum::toString)
          .collect(Collectors.joining(","));
    }
    if (field.getName().equals("usefulState")) {
      return usefulState.toString();
    }
    return super.encodeField(value, field);
  }

  @Override
  protected Object decodeField(final Object value, final Field field) {
    if (field.getName().equals("root")) {
      return GSON.fromJson(((String) value), Node.class);
    }
    if (field.getName().equals("urls")) {
      return Lists.newArrayList(((String) value).split("==="));
    }
    if (field.getName().equals("tags")) {
      return Arrays.stream(((String) value).split(","))
          .filter(e -> !e.isEmpty())
          .map(Tag::valueOf)
          .collect(Collectors.toList());
    }
    if (field.getName().equals("usefulState")) {
      return UsefulState.valueOf(value.toString());
    }
    return super.decodeField(value, field);
  }

  public enum UsefulState {
    USEFUL, NOT_USEFUL, NONE
  }
}

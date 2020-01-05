package com.github.kusumotolab.tc2p.core.entities;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.List;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteColumn;
import com.github.kusumotolab.tc2p.tools.db.sqlite.SQLiteObject;
import com.github.kusumotolab.tc2p.tools.gson.GsonFactory;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

  @SQLiteColumn(type = Types.CHAR)
  private Node<ASTLabel> root;

  @SQLiteColumn(type = Types.CHAR)
  private List<String> urls;

  @SQLiteColumn(type = Types.BOOLEAN, name = "is_deleted")
  private boolean isDeleted = false;

  @SQLiteColumn(type = Types.CHAR)
  private String name;

  @SQLiteColumn(type = Types.CHAR)
  private String comment;

  private static final Gson GSON = GsonFactory.create();

  public MiningResult(final int id, final String projectName, final int frequency, final int maxDepth, final int size,
      final Node<ASTLabel> root, final List<String> urls) {
    this.id = id;
    this.projectName = projectName;
    this.frequency = frequency;
    this.maxDepth = maxDepth;
    this.size = size;
    this.root = root;
    this.urls = urls;
  }

  @Override
  protected Object encodeField(final Object value, final Field field) {
    if (field.getName().equals("root")) {
      return GSON.toJson(value);
    }

    if (field.getName().equals("urls")) {
      return String.join("===", urls);
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
    return super.decodeField(value, field);
  }
}

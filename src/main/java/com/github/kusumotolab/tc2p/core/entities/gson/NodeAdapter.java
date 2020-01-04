package com.github.kusumotolab.tc2p.core.entities.gson;


import java.io.IOException;
import java.util.List;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.google.common.collect.Lists;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class NodeAdapter extends TypeAdapter<Node<ASTLabel>> {

  @Override
  public void write(final JsonWriter out, final Node<ASTLabel> value) throws IOException {
    out.beginArray();
    for (final Label<ASTLabel> label : value.getLabels()) {
      out.beginObject();
      out.name("depth").value(label.getDepth());
      final ASTLabel astLabel = label.getLabel();
      out.name("id").value(astLabel.getId());
      out.name("parentId").value(astLabel.getParentId());
      final JsonWriter actionWriter = out.name("actions").beginArray();
      for (final ActionEnum action : astLabel.getActions()) {
        actionWriter.value(action.toString());
      }
      actionWriter.endArray();
      out.name("value").value(astLabel.getValue());
      out.name("newValue").value(astLabel.getNewValue());
      out.name("type").value(astLabel.getType());
      out.endObject();
    }
    out.endArray();
  }

  @Override
  public Node<ASTLabel> read(final JsonReader in) throws IOException {
    final List<Label<ASTLabel>> astLabels = Lists.newArrayList();
    in.beginArray();
    while (in.hasNext()) {
      in.beginObject();
      in.nextName();
      int depth = in.nextInt();
      int id = -1;
      int parentId = -1;
      final List<ActionEnum> actionEnums = Lists.newArrayList();
      String value = null;
      String newValue = null;
      String type = null;
      while (in.hasNext()) {
        switch (in.nextName()) {
          case "id":
            id = in.nextInt();
            break;
          case "parentId":
            parentId = in.nextInt();
            break;
          case "actions":
            in.beginArray();
            while (in.hasNext()) {
              actionEnums.add(ActionEnum.valueOf(in.nextString()));
            }
            in.endArray();
            break;
          case "value":
            value = in.nextString();
            break;
          case "newValue":
            newValue = in.nextString();
            break;
          case "type":
            type = in.nextString();
        }
      }
      astLabels.add(new Label<>(depth, new ASTLabel(id, parentId, actionEnums, value, newValue, type)));
      in.endObject();
    }
    in.endArray();
    return Node.createTree("", astLabels);
  }
}
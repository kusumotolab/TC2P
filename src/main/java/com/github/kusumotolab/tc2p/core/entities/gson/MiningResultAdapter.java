package com.github.kusumotolab.tc2p.core.entities.gson;

import java.io.IOException;
import java.util.List;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.MiningResult.UsefulState;
import com.github.kusumotolab.tc2p.core.entities.PatternPosition;
import com.github.kusumotolab.tc2p.utils.patternmining.itembag.ParallelItemBag;
import com.google.common.collect.Lists;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class MiningResultAdapter extends TypeAdapter<MiningResult> {

  @Override
  public void write(final JsonWriter out, final MiningResult value) throws IOException {
    out.beginObject();
    out.name("id").value(value.getId());
    out.name("project_name").value(value.getProjectName());
    out.name("frequency").value(value.getFrequency());
    out.name("max_depth").value(value.getMaxDepth());
    out.name("size").value(value.getSize());
    new NodeAdapter().write(out.name("node"), value.getRoot());
    final JsonWriter urls = out.name("urls").beginArray();
    for (final PatternPosition position : value.getPatternPositions()) {
      out.beginObject();
      out.name("url").value(position.getUrl());
      out.name("mjava_diff").value(position.getMjavaDiff());
    }
    urls.endArray();
    urls.name("is_deleted").value(value.isDeleted());
    urls.name("name").value(value.getName());
    urls.name("comment").value(value.getComment());
    out.endObject();
  }

  @Override
  public MiningResult read(final JsonReader in) throws IOException {
    in.beginObject();
    in.nextName();
    final int id = in.nextInt();

    in.nextName();
    final String projectName = in.nextString();

    in.nextName();
    final int frequency = in.nextInt();

    in.nextName();
    final int maxDepth = in.nextInt();

    in.nextName();
    final int size = in.nextInt();

    in.nextName();
    final Node<ASTLabel> node = new NodeAdapter().read(in);

    in.nextName();
    final List<PatternPosition> patternPositions = Lists.newArrayList();

    in.beginArray();
    while (in.hasNext()) {
      in.beginObject();
      in.nextName();
      final String url = in.nextString();
      in.nextName();
      final String mjavadiff = in.nextString();
      patternPositions.add(new PatternPosition(url, mjavadiff));
      in.endObject();
    }
    in.endArray();

    in.nextName();
    final boolean isDeleted = in.nextBoolean();

    in.nextName();
    String name = null;
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
    } else {
      name = in.nextString();
    }

    in.nextName();
    String comment = null;
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
    } else {
      comment = in.nextString();
    }

    in.nextName();
    final String state = in.nextString();

    in.endObject();
    return new MiningResult(id, projectName, frequency, maxDepth, size, node, patternPositions, isDeleted, name, comment, UsefulState.valueOf(state));
  }

}

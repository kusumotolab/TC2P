package com.github.kusumotolab.tc2p.tools.gson;

import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.entities.gson.MiningResultAdapter;
import com.github.kusumotolab.tc2p.core.entities.gson.NodeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {

  public static Gson create() {
    return new GsonBuilder().registerTypeAdapter(Node.class, new NodeAdapter())
        .registerTypeAdapter(MiningResult.class, new MiningResultAdapter())
        .serializeNulls()
        .create();
  }
}

package com.github.kusumotolab.tc2p.core.usecase.interactor;

import java.util.List;
import java.util.Map;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Label;
import com.github.kusumotolab.sdl4j.algorithm.mining.tree.Node;
import com.github.kusumotolab.tc2p.core.entities.ASTLabel;
import com.github.kusumotolab.tc2p.core.entities.ActionEnum;
import com.github.kusumotolab.tc2p.core.entities.MiningResult;
import com.github.kusumotolab.tc2p.core.usecase.interactor.MiningEditPatternResultParser.Input;
import com.github.kusumotolab.tc2p.utils.Colors;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

public class MiningEditPatternResultParser implements Interactor<Input, List<MiningResult>> {

  @Override
  public List<MiningResult> execute(final Input input) {
    final List<MiningResult> results = Lists.newArrayList();

    String projectName = "NoName";
    int frequency = 0;
    int maxDepth = 0;
    List<String> urls = Lists.newArrayList();
    List<Label<ASTLabel>> astLabels = Lists.newArrayList();
    int nodeId = 0;
    int patternId = 0;
    final Map<Integer, Integer> depthIoId = Maps.newHashMap();

    for (final String _line : input.lines) {
      final String line = Colors.removeColorCode(_line);

      if (line.startsWith("java -jar")) {
        projectName = line.split(" ")[6];
        continue;
      }

      if (line.contains("ConsoleView - Frequency:")) {
        final String[] split = line.split(" ");
        frequency = Integer.parseInt(split[split.length - 1]);
        urls = Lists.newArrayList();
        astLabels = Lists.newArrayList();
        continue;
      }


      if (line.contains("type={")) {
        final String[] split = line.trim().split("\\s+");
        final int depth = Integer.parseInt(split[0]);
        maxDepth = Math.max(depth, maxDepth);
        String type = "";
        String newValue = "";
        String value = "";
        final List<ActionEnum> actionEnums = Lists.newArrayList();

        final String[] strings = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split("}, ");
        for (final String string : strings) {
          String text = string;
          if (!text.endsWith("}")) {
            text += "}";
          }
          if (text.startsWith("(")) {
            text = text.substring(1);
          }
          if (text.endsWith(")")) {
            text = text.substring(0, text.lastIndexOf(")"));
          }
          if (text.endsWith(",")) {
            text = text.substring(0, text.lastIndexOf(","));
          }

          final int index = text.indexOf("=");
          final String name = text.substring(0, index);
          final String valueText = text.substring(index + 1);
          if (name.equals("actions")) {
            final String[] actionNames = valueText.substring(1, valueText.length() - 1).split(", ");
            for (final String actionName : actionNames) {
              actionEnums.add(ActionEnum.valueOf(actionName));
            }
          } else if (name.equals("type")) {
            type = valueText.substring(2, valueText.length() - 2);
          } else if (name.equals("value")) {
            value = valueText.substring(2, valueText.length() - 2);
          } else if (name.equals("newValue")) {
            newValue = valueText.substring(2, valueText.length() - 2);
          }
        }
        nodeId += 1;
        depthIoId.put(depth, nodeId);
        final int parentId = depthIoId.getOrDefault(depth - 1, -1);
        astLabels.add(new Label<>(depth, new ASTLabel(nodeId, parentId, actionEnums, value, newValue, type)));
        continue;
      }

      if (line.contains("https://github.com")) {
        urls.add(line.split(" ")[1]);
        continue;
      }

      if (line.isEmpty()) {
        if (astLabels.isEmpty()) {
          continue;
        }
        final MiningResult miningResult = new MiningResult(patternId, projectName, frequency, maxDepth, nodeId, Node.createTree(projectName, astLabels),
            urls);
        results.add(miningResult);
        patternId += 1;
        frequency = 0;
        maxDepth = 0;
        nodeId = 0;
        astLabels = Lists.newArrayList();
        urls = Lists.newArrayList();
      }

    }

    return results;
  }

  @Data
  public static class Input {
    private final List<String> lines;
  }
}


package com.github.kusumotolab.tc2p;

import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.service.ServiceProvider;

public class Main {

  public static void main(final String[] args) {
    final Controller service = ServiceProvider.getInstance()
        .resolve(args[0]);
    service.exec(removedCommand(args));
  }

  private static String[] removedCommand(final String[] args) {
    final String[] removedArgs = new String[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      removedArgs[i - 1] = args[i];
    }
    return removedArgs;
  }
}

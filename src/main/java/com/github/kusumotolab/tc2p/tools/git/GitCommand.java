package com.github.kusumotolab.tc2p.tools.git;

import org.eclipse.jgit.lib.Repository;

public abstract class GitCommand<Input, Output> {

  protected final Repository repository;

  public GitCommand(final Repository repository) {
    this.repository = repository;
  }

  public abstract Output execute(final Input input);

}

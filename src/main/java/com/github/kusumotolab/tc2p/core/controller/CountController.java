package com.github.kusumotolab.tc2p.core.controller;

import com.github.kusumotolab.tc2p.core.configuration.CountUsefulConfiguration;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;

public class CountController<V extends View, P extends Presenter<V>, U extends UseCase<CountUsefulConfiguration, V, P>> extends Controller<V, P, U> {

  public CountController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new CountUsefulConfiguration(), args, useCase::execute);
  }
}

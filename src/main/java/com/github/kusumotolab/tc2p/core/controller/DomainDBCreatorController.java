package com.github.kusumotolab.tc2p.core.controller;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import com.github.kusumotolab.tc2p.core.configuration.DomainDBCreateConfiguration;
import com.github.kusumotolab.tc2p.core.usecase.IDomainDBCreateUseCase;
import com.github.kusumotolab.tc2p.core.usecase.IDomainDBCreateUseCase.Input;
import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.View;
import com.github.kusumotolab.tc2p.utils.Try;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DomainDBCreatorController<V extends View, P extends Presenter<V>, U extends IDomainDBCreateUseCase<V, P>> extends Controller<V, P, U> {

  public DomainDBCreatorController(final U useCase) {
    super(useCase);
  }

  @Override
  public void exec(final String[] args) {
    parse(new DomainDBCreateConfiguration(), args, configuration -> {
      final String jsonContents = Try.force(() -> Files.readString(configuration.getJsonPath()));
      final Type collectionType = new TypeToken<List<Input>>(){}.getType();
      final List<Input> list = new Gson().fromJson(jsonContents, collectionType);
      for (final Input input : list) {
        useCase.execute(input);
      }
    });
  }
}

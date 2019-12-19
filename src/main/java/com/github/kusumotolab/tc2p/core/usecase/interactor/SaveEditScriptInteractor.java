package com.github.kusumotolab.tc2p.core.usecase.interactor;

import com.github.kusumotolab.tc2p.core.entities.EditScript;
import com.github.kusumotolab.tc2p.core.usecase.interactor.SaveEditScriptInteractor.Input;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeInput;
import com.github.kusumotolab.tc2p.tools.gumtree.GumTreeOutput;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class SaveEditScriptInteractor implements Interactor<Input, Completable> {

  public static class Input {
    private final Observable<GumTreeOutput> gumTreeOutput;

    public Input(final Observable<GumTreeOutput> gumTreeOutput) {
      this.gumTreeOutput = gumTreeOutput;
    }
  }

  @Override
  public Completable execute(final Input input) {
    final Observable<GumTreeOutput> gumTreeOutput = input.gumTreeOutput;
    /*
    gumTreeOutput.map(output -> {
      final EditScript editScript = new EditScript();
      editScript.
    });
     */
    return null;
  }

  private EditScript createEditScript(final GumTreeOutput gumTreeOutput) {
    final GumTreeInput gumTreeInput = gumTreeOutput.getInput();
    final EditScript editScript = new EditScript();
    editScript.setSrcName(gumTreeInput.getSrcPath());
//    editScript.setSrcCommitID(gumTreeInput.);
    return editScript;
  }
}

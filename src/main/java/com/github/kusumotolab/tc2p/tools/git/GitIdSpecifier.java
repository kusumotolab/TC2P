package com.github.kusumotolab.tc2p.tools.git;

import java.util.function.Consumer;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import io.reactivex.Maybe;

public class GitIdSpecifier extends GitCommand<Consumer<TreeWalk>, Maybe<ObjectId>> {

  public GitIdSpecifier(final Repository repository) {
    super(repository);
  }

  @Override
  public Maybe<ObjectId> execute(final Consumer<TreeWalk> setting) {
    return Maybe.create(emitter -> {
      try (final TreeWalk treeWalk = new TreeWalk(repository)) {
        setting.accept(treeWalk);
        if (!treeWalk.next()) {
          emitter.onComplete();
        }
        final ObjectId objectId = treeWalk.getObjectId(0);
        emitter.onSuccess(objectId);
      } catch (final Exception e) {
        emitter.onError(e);
      }
    });
  }
}

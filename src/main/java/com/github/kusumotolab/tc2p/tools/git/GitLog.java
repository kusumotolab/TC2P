package com.github.kusumotolab.tc2p.tools.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import com.github.kusumotolab.tc2p.utils.Try;
import io.reactivex.Observable;

public class GitLog extends GitCommand<String, Observable<RevCommit>> {

  public GitLog(final Repository repository) {
    super(repository);
  }

  @Override
  public Observable<RevCommit> execute(final String id) {
    return Observable.create(emitter -> {
      final Iterable<RevCommit> call = Try.force(() -> new Git(repository).log().add(ObjectId.fromString(id)).call());
      call.forEach(emitter::onNext);
      emitter.onComplete();
    });
  }
}

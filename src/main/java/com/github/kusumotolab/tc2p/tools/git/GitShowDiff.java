package com.github.kusumotolab.tc2p.tools.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import com.github.kusumotolab.tc2p.core.entities.CommitPair;
import com.github.kusumotolab.tc2p.core.entities.FileRevision;
import com.github.kusumotolab.tc2p.core.entities.FileRevision.FileRef;
import io.reactivex.Observable;

public class GitShowDiff extends GitCommand<CommitPair, Observable<FileRevision>> {

  public GitShowDiff(final Repository repository) {
    super(repository);
  }

  @Override
  public Observable<FileRevision> execute(final CommitPair commitPair) {
    return Observable.create(emitter -> {
      final RevCommit srcCommit = commitPair.getSrcCommit();
      final RevCommit dstCommit = commitPair.getDstCommit();

      try (ObjectReader reader = repository.newObjectReader()) {
        final CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, srcCommit.getId());
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, dstCommit.getId());

        try (Git git = new Git(repository)) {
          git.diff()
              .setNewTree(newTreeIter)
              .setOldTree(oldTreeIter)
              .call()
              .stream()
              .filter(e -> e.getChangeType() != ChangeType.ADD && e.getChangeType() != ChangeType.DELETE)
              .map(e -> {
                final String srcPath = e.getOldPath();
                final String dstPath = e.getNewPath();
                return new FileRevision(new FileRef(srcPath, srcCommit), new FileRef(dstPath, dstCommit));
              }).forEach(emitter::onNext);
          emitter.onComplete();
        }
      }
    });
  }
}

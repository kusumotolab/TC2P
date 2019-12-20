package com.github.kusumotolab.tc2p.tools.git;

import java.io.ByteArrayOutputStream;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import com.github.kusumotolab.tc2p.tools.git.GitCatBlob.GitBlobInput;
import com.github.kusumotolab.tc2p.utils.Try;
import io.reactivex.Maybe;
import lombok.Data;

public class GitCatBlob extends GitCommand<GitBlobInput, Maybe<String>> {

  public GitCatBlob(final Repository repository) {
    super(repository);
  }

  @Override
  public Maybe<String> execute(final GitBlobInput input) {
    return new GitIdSpecifier(repository).execute(treeWalk -> {
      final RevCommit commit = input.getCommit();
      Try.force(() -> treeWalk.addTree(commit.getTree()));
      treeWalk.setRecursive(true);
      treeWalk.setFilter(PathFilter.create(input.getPath()));
    })
        .map(repository::open)
        .map(loader -> {
          try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            loader.copyTo(outputStream);
            return outputStream.toString();
          }
        });
  }

  @Data
  public static class GitBlobInput {

    private final String path;
    private final RevCommit commit;
  }
}

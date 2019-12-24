package com.github.kusumotolab.tc2p.tools.gumtree;

import java.nio.file.Path;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jgit.revwalk.RevCommit;
import com.github.kusumotolab.tc2p.core.entities.CommitPair;
import com.github.kusumotolab.tc2p.core.entities.FileRevision.FileRef;
import com.github.kusumotolab.tc2p.tools.git.GitClient;
import io.reactivex.Observable;

public class GumTreeAdapter {

  private final Path repositoryPath;

  public GumTreeAdapter(final Path repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  public Observable<GumTreeInput> convert(final CommitPair commitLogPair) {
    return GitClient.create(repositoryPath)
        .map(gitClient -> gitClient.show(commitLogPair)
            .map(fileRevision -> {
              final FileRef srcFileRef = fileRevision.getSrc();
              final FileRef dstFileRef = fileRevision.getDst();

              return createGumTreeInput(srcFileRef.getName(), dstFileRef.getName(),
                  getContents(srcFileRef, gitClient), getContents(dstFileRef, gitClient));
            })
        )
        .orElse(Observable.empty());
  }

  private String getContents(final FileRef fileRef, final GitClient gitClient) {
    final RevCommit commit = fileRef.getCommit();
    final String fileName = fileRef.getName();

    final String content = gitClient.catBlob(commit, fileName).blockingGet();
    if (content == null) {
      return "class XXX {}";
    }

    final String classContent = fileName.endsWith(".java") ? content
        : fileName.endsWith(".mjava") ? "class XXX { " + content + "}"
            : "class XXX {}";
    return removeJavaDoc(classContent);
  }

  private GumTreeInput createGumTreeInput(final String srcPath, final String dstPath,
      final String srcContents, final String dstContents) {
    return new GumTreeInput(srcPath, dstPath, srcContents, dstContents);
  }

  private String removeJavaDoc(final String methodText) {
    ASTParser parser = ASTParser.newParser(AST.JLS11);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    final Map<String, String> pOptions = JavaCore.getOptions();
    pOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
    pOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
    pOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
    pOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
    parser.setCompilerOptions(pOptions);
    parser.setSource(methodText.toCharArray());
    final ASTNode ast = parser.createAST(null);
    ast.accept(new ASTVisitor() {
      @Override
      public boolean visit(final Javadoc node) {
        node.delete();
        return false;
      }
    });
    return ast.toString();
  }
}

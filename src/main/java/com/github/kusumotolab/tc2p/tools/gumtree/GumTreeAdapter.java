package com.github.kusumotolab.tc2p.tools.gumtree;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jgit.revwalk.RevCommit;
import com.github.kusumotolab.tc2p.core.entities.CommitLogPair;
import com.github.kusumotolab.tc2p.tools.git.CommitLog;
import com.github.kusumotolab.tc2p.tools.git.GitClient;

public class GumTreeAdapter {

  private final Path repositoryPath;

  public GumTreeAdapter(final Path repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  public Optional<GumTreeInput> convert(final CommitLogPair commitLogPair) {
    return GitClient.create(repositoryPath)
        .map(gitClient -> {
          final CommitLog srcCommitLog = commitLogPair.getSrcCommitLog();
          final CommitLog dstCommitLog = commitLogPair.getDstCommitLog();

          final String srcContents = getContents(srcCommitLog, gitClient);
          final String dstContents = getContents(dstCommitLog, gitClient);

          return createGumTreeInput(srcCommitLog.getFileName(), dstCommitLog.getFileName(),
              srcContents, dstContents);
        });
  }

  private String getContents(final CommitLog commitLog, final GitClient gitClient) {
    final RevCommit commit = commitLog.getCommit();
    final String filePath = commitLog.getFileName()
        .toString();

    final String content = gitClient.catBlob(commit, filePath).blockingGet();
    if (content == null) {
      return "class XXX {}";
    }

    final String javaDocRemovedContent = removeJavaDoc(content);
    return filePath.endsWith(".java") ? javaDocRemovedContent
        : filePath.endsWith(".mjava") ? "class XXX { " + javaDocRemovedContent + "}"
            : "class XXX {}";
  }

  private GumTreeInput createGumTreeInput(final Path srcPath, final Path dstPath,
      final String srcContents, final String dstContents) {
    return new GumTreeInput(srcPath.toString(), dstPath.toString(), srcContents, dstContents);
  }

  private String removeJavaDoc(final String methodText) {
    ASTParser parser = ASTParser.newParser(AST.JLS11);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    Map pOptions = JavaCore.getOptions();
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

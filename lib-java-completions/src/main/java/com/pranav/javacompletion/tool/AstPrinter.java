package com.pranav.javacompletion.tool;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Joiner;
import com.pranav.javacompletion.parser.FileContentFixer;
import com.pranav.javacompletion.parser.ParserContext;

import org.openjdk.source.tree.ClassTree;
import org.openjdk.source.tree.ErroneousTree;
import org.openjdk.source.tree.IdentifierTree;
import org.openjdk.source.tree.MemberSelectTree;
import org.openjdk.source.tree.MethodInvocationTree;
import org.openjdk.source.tree.MethodTree;
import org.openjdk.source.tree.ModifiersTree;
import org.openjdk.source.tree.Tree;
import org.openjdk.source.tree.TypeParameterTree;
import org.openjdk.source.tree.VariableTree;
import org.openjdk.source.tree.WildcardTree;
import org.openjdk.source.util.TreeScanner;
import org.openjdk.tools.javac.tree.JCTree.JCCompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Print AST tree produced by javac parser.
 *
 * <p>Usage:
 *
 * <pre>
 * bazel run //src/main/java/org/javacomp/tool:AstPrinter -- [-f] <java-file-name>
 * </pre>
 */
public class AstPrinter extends TreeScanner<Void, Void> {
    private final FileContentFixer fileContentFixer;
    private final ParserContext parserContext;

    private int indent;

    public AstPrinter() {
        this.indent = 0;
        this.parserContext = new ParserContext();
        this.fileContentFixer = new FileContentFixer(this.parserContext);
    }

    public void scan(String filename, boolean fixFileContent) {
        try {
            CharSequence content = new String(Files.readAllBytes(Paths.get(filename)), UTF_8);
            parserContext.setupLoggingSource(filename);

            if (fixFileContent) {
                content = fileContentFixer.fixFileContent(content).getContent();
                System.out.println("Updated content:");
                System.out.println(content);
            }
            JCCompilationUnit compilationUnit = parserContext.parse(filename, content.toString());
            scan(compilationUnit, null);
            System.out.println("");
        } catch (IOException e) {
            System.exit(1);
        }
    }

    @Override
    public Void scan(Tree node, Void unused) {
        if (node == null) {
            System.out.print(" <null>");
            return null;
        }
        printIndent();
        System.out.print(node.getClass().getSimpleName());
        indent += 2;
        super.scan(node, null);
        indent -= 2;
        return null;
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree node, Void unused) {
        System.out.print(" " + node.getIdentifier().toString());
        super.visitMemberSelect(node, unused);
        return null;
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, Void unused) {
        System.out.print(" " + node.getName());
        return null;
    }

    @Override
    public Void visitModifiers(ModifiersTree node, Void unused) {
        System.out.print(" " + Joiner.on(", ").join(node.getFlags()));
        super.visitModifiers(node, null);
        return null;
    }

    @Override
    public Void visitErroneous(ErroneousTree node, Void unused) {
        scan(node.getErrorTrees(), null);
        return null;
    }

    @Override
    public Void visitClass(ClassTree node, Void unused) {
        System.out.print(" " + node.getSimpleName());
        scan(node.getModifiers(), null);
        printWithIndent("[type parameters]:");
        scan(node.getTypeParameters(), null);
        printWithIndent("[extend clause]:");
        scan(node.getExtendsClause(), null);
        printWithIndent("[implements clause]:");
        scan(node.getImplementsClause(), null);
        printWithIndent("[members]:");
        scan(node.getMembers(), null);
        return null;
    }

    @Override
    public Void visitWildcard(WildcardTree node, Void unused) {
        printWithIndent("Kind: " + node.getKind());
        if (node.getBound() != null) {
            scan(node.getBound(), null);
        }
        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterTree node, Void unused) {
        System.out.print(" " + node.getName());
        printWithIndent("[annotations]");
        scan(node.getAnnotations(), null);
        printWithIndent("[bounds]");
        scan(node.getBounds(), null);
        return null;
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        System.out.print(" " + node.getName());
        printWithIndent("[modifiers]");
        scan(node.getModifiers(), null);
        printWithIndent("[return type]");
        scan(node.getReturnType(), null);
        printWithIndent("[type parameters]");
        scan(node.getTypeParameters(), null);
        printWithIndent("[parameters]");
        scan(node.getParameters(), null);
        printWithIndent("[receiver parameter]");
        scan(node.getReceiverParameter(), null);
        printWithIndent("[throws]");
        scan(node.getThrows(), null);
        printWithIndent("[body]");
        scan(node.getBody(), null);
        printWithIndent("[default value]");
        scan(node.getDefaultValue(), null);
        return null;
    }

    @Override
    public Void visitVariable(VariableTree node, Void unused) {
        System.out.print(" " + node.getName());
        printWithIndent("[modifiers]");
        scan(node.getModifiers(), null);
        printWithIndent("[type]");
        scan(node.getType(), null);
        printWithIndent("[name expression]");
        scan(node.getNameExpression(), null);
        printWithIndent("[initializer]");
        scan(node.getInitializer(), null);
        return null;
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
        scan(node.getMethodSelect(), null);
        printWithIndent("[arguments]");
        scan(node.getArguments(), null);
        printWithIndent("[type arguments]");
        scan(node.getTypeArguments(), null);
        return null;
    }

    private void printWithIndent(String suffix) {
        printIndent();
        System.out.print(suffix);
    }

    private void printIndent() {
        System.out.println("");
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            printHelp();
        }
        boolean fixFileContent = false;
        String filename;
        if ("-f".equals(args[0])) {
            if (args.length < 2) {
                printHelp();
            }
            fixFileContent = true;
            filename = args[1];
        } else {
            filename = args[0];
        }
        new AstPrinter().scan(filename, fixFileContent);
    }

    private static void printHelp() {
        System.out.println("Usage: AstPrinter <javafile>");
        System.exit(1);
    }
}

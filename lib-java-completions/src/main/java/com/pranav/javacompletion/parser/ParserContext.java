package com.pranav.javacompletion.parser;

import static com.google.common.base.Charsets.UTF_8;

import static org.openjdk.tools.javac.tree.JCTree.JCCompilationUnit;

import org.openjdk.tools.javac.file.JavacFileManager;
import org.openjdk.tools.javac.parser.JavacParser;
import org.openjdk.tools.javac.parser.ParserFactory;
import org.openjdk.tools.javac.parser.Scanner;
import org.openjdk.tools.javac.parser.ScannerFactory;
import org.openjdk.tools.javac.util.Context;
import org.openjdk.tools.javac.util.Log;

/** Environment for using Javac Parser */
public class ParserContext {
    private final Context javacContext;
    private final JavacFileManager javacFileManager;

    public ParserContext() {
        javacContext = new Context();
        javacFileManager = new JavacFileManager(javacContext, true /* register */, UTF_8);
    }

    /**
     * Set source file of the log.
     *
     * <p>This method should be called before parsing or lexing. If not set,
     * IllegalArgumentException will be thrown if the parser enconters errors.
     */
    public void setupLoggingSource(String filename) {
        SourceFileObject sourceFileObject = new SourceFileObject(filename);
        Log javacLog = Log.instance(javacContext);
        javacLog.useSource(sourceFileObject);
    }

    /**
     * Parses the content of a Java file.
     *
     * @param filename the filename of the Java file
     * @param content the content of the Java file
     */
    public JCCompilationUnit parse(String filename, CharSequence content) {
        setupLoggingSource(filename);

        // Create a parser and start parsing.
        JavacParser parser =
                ParserFactory.instance(javacContext)
                        .newParser(
                                content,
                                true /* keepDocComments */,
                                true /* keepEndPos */,
                                true /* keepLineMap */);
        return parser.parseCompilationUnit();
    }

    public Scanner tokenize(CharSequence content, boolean keepDocComments) {
        return ScannerFactory.instance(javacContext).newScanner(content, keepDocComments);
    }
}

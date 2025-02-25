package com.pranav.javacompletion.parser;

import org.openjdk.javax.tools.SimpleJavaFileObject;

import java.nio.file.Paths;

/** A {@link SimpleJavaFileObject} for Java source code. */
public class SourceFileObject extends SimpleJavaFileObject {
    /**
     * @param filename the absolute path of the source file.
     */
    public SourceFileObject(String filename) {
        super(Paths.get(filename).toUri(), Kind.SOURCE);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return "";
    }
}

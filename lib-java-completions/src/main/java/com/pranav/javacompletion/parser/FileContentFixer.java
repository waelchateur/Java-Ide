package com.pranav.javacompletion.parser;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

import org.openjdk.tools.javac.parser.Scanner;
import org.openjdk.tools.javac.parser.Tokens.Token;
import org.openjdk.tools.javac.parser.Tokens.TokenKind;
import org.openjdk.tools.javac.util.Position.LineMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Correct the content of a Java file for completion. */
public class FileContentFixer {
    private static final Set<TokenKind> VALID_MEMBER_SELECTION_TOKENS =
            ImmutableSet.of(
                    TokenKind.IDENTIFIER,
                    TokenKind.LT,
                    TokenKind.NEW,
                    TokenKind.THIS,
                    TokenKind.SUPER,
                    TokenKind.CLASS,
                    TokenKind.STAR);
    /** Token kinds that can not be right after a memeber selection. */
    private static final Set<TokenKind> INVALID_MEMBER_SELECTION_SUFFIXES =
            ImmutableSet.of(TokenKind.RBRACE);

    private final ParserContext parserContext;

    public FileContentFixer(ParserContext parserContext) {
        this.parserContext = parserContext;
    }

    public FixedContent fixFileContent(CharSequence content) {
        Scanner scanner = parserContext.tokenize(content, false /* keepDocComments */);
        List<Insertion> insertions = new ArrayList<>();
        for (; ; scanner.nextToken()) {
            Token token = scanner.token();
            if (token.kind == TokenKind.EOF) {
                break;
            } else if (token.kind == TokenKind.DOT || token.kind == TokenKind.COLCOL) {
                fixMemberSelection(scanner, insertions);
            } else if (token.kind == TokenKind.ERROR) {
                int errPos = scanner.errPos();
                if (errPos >= 0 && errPos < content.length()) {
                    fixError(scanner, content, insertions);
                }
            }
        }
        CharSequence modifiedContent = Insertion.applyInsertions(content, insertions);
        return FixedContent.create(
                modifiedContent, createAdjustedLineMap(scanner.getLineMap(), insertions));
    }

    private void fixMemberSelection(Scanner scanner, List<Insertion> insertions) {
        Token token = scanner.token();
        Token nextToken = scanner.token(1);

        LineMap lineMap = scanner.getLineMap();
        int tokenLine = lineMap.getLineNumber(token.pos);
        int nextLine = lineMap.getLineNumber(nextToken.pos);

        if (nextLine > tokenLine) {
            // The line ends with a dot. It's likely the user is entering a dot and waiting for
            // member
            // completion. The current line is incomplete and syntextually invalid.
            insertions.add(Insertion.create(token.endPos, "dumbIdent;"));
        } else if (!VALID_MEMBER_SELECTION_TOKENS.contains(nextToken.kind)) {
            String toInsert = "dumbIdent";
            if (INVALID_MEMBER_SELECTION_SUFFIXES.contains(nextToken.kind)) {
                toInsert = "dumbIdent;";
            }

            // The member selection is syntextually invalid. Fix it.
            insertions.add(Insertion.create(token.endPos, toInsert));
        }
    }

    private void fixError(Scanner scanner, CharSequence content, List<Insertion> insertions) {
        int errPos = scanner.errPos();
        if (content.charAt(errPos) == '.' && errPos > 0 && content.charAt(errPos) == '.') {
            // The scanner fails at two dots because it expects three dots for
            // ellipse. The errPos is at the second dot.
            //
            // If the second dot is followed by an identifier character, it's likely
            // the user is trying to complete between the two dots. Otherwise, the
            // user is likely in the process of typing the third dot.
            if (errPos < content.length() - 1
                    && Character.isJavaIdentifierStart(content.charAt(errPos + 1))) {
                // Insert a dumbIdent between two dots so the Javac parser can parse it.
                insertions.add(Insertion.create(errPos, "dumbIdent"));
            }
        }
    }

    private AdjustedLineMap createAdjustedLineMap(
            LineMap originalLineMap, List<Insertion> insertions) {
        return new AdjustedLineMap.Builder()
                .setOriginalLineMap(originalLineMap)
                .addInsertions(insertions)
                .build();
    }

    @AutoValue
    public abstract static class FixedContent {
        public abstract String getContent();

        public abstract org.openjdk.source.tree.LineMap getAdjustedLineMap();

        public static FixedContent create(CharSequence content, AdjustedLineMap lineMap) {
            return new AutoValue_FileContentFixer_FixedContent(content.toString(), lineMap);
        }
    }
}

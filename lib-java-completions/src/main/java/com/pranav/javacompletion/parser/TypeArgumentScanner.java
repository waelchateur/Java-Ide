package com.pranav.javacompletion.parser;

import com.pranav.javacompletion.logging.JLogger;
import com.pranav.javacompletion.model.TypeArgument;
import com.pranav.javacompletion.model.WildcardTypeArgument;

import org.openjdk.source.tree.Tree;
import org.openjdk.source.tree.WildcardTree;
import org.openjdk.source.util.TreeScanner;

import java.util.Optional;

/** Converts a Java source tree to a {@link TypeArgument}. */
public class TypeArgumentScanner extends TreeScanner<TypeArgument, Void> {
    private static final JLogger logger = JLogger.createForEnclosingClass();

    public TypeArgument getTypeArgument(Tree node) {
        return scan(node, null);
    }

    @Override
    public TypeArgument scan(Tree node, Void unused) {
        if (node instanceof WildcardTree) {
            return createWildcardTypeArgument((WildcardTree) node);
        }
        return new TypeReferenceScanner(this).getTypeReference(node);
    }

    private WildcardTypeArgument createWildcardTypeArgument(WildcardTree node) {
        Optional<WildcardTypeArgument.Bound> bound;
        switch (node.getKind()) {
            case SUPER_WILDCARD:
                bound =
                        Optional.of(
                                WildcardTypeArgument.Bound.create(
                                        WildcardTypeArgument.Bound.Kind.SUPER,
                                        new TypeReferenceScanner()
                                                .getTypeReference(node.getBound())));
                break;
            case EXTENDS_WILDCARD:
                bound =
                        Optional.of(
                                WildcardTypeArgument.Bound.create(
                                        WildcardTypeArgument.Bound.Kind.EXTENDS,
                                        new TypeReferenceScanner()
                                                .getTypeReference(node.getBound())));
                break;
            case UNBOUNDED_WILDCARD:
                bound = Optional.empty();
                break;
            default:
                logger.warning("Unknown wildcard type varialbe kind: %s", node.getKind());
                bound = Optional.empty();
        }
        return WildcardTypeArgument.create(bound);
    }
}

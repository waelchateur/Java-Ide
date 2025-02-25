package com.pranav.javacompletion.typesolver;

import com.google.common.collect.ImmutableList;
import com.pranav.javacompletion.model.Entity;
import com.pranav.javacompletion.model.EntityScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A build of {@link List} that ignores a new element the list if any {@link Entity} from existing
 * elements can shadow the {@link Entity} from the new element.
 *
 * <p>An {@link Entity} a can shadow another {@link Entity} b if:
 *
 * <ul>
 *   <li>Neither a nor b is a method; or
 *   <li>Both a and b are methods, and a can override b.
 * </ul>
 */
public class EntityShadowingListBuilder<E> {
    private final ArrayList<E> elements;
    private final Function<E, Entity> getEntityFunction;

    /**
     * @param getEntityFunction a function to get an {@link Entity} from an element for shadowing
     *     check. If the function returns {@code null}, the element won't shadow any other element
     *     and won't be shadowed
     */
    public EntityShadowingListBuilder(Function<E, Entity> getEntityFunction) {
        this.elements = new ArrayList<>();
        this.getEntityFunction = getEntityFunction;
    }

    public EntityShadowingListBuilder<E> add(E newElement) {
        Entity newEntity = getEntityFunction.apply(newElement);
        for (E existing : elements) {
            if (entityShadows(getEntityFunction.apply(existing), newEntity)) {
                return this;
            }
        }
        elements.add(newElement);
        return this;
    }

    public ImmutableList<E> build() {
        return ImmutableList.copyOf(elements);
    }

    public Stream<E> stream() {
        return elements.stream();
    }

    private boolean entityShadows(Entity existingEntity, Entity newEntity) {
        if (existingEntity == null || newEntity == null) {
            return false;
        }
        boolean existingIsMethod = existingEntity.getKind() == Entity.Kind.METHOD;
        boolean newIsMethod = newEntity.getKind() == Entity.Kind.METHOD;
        boolean existingIsForImport = existingEntity instanceof ForImportEntity;
        boolean newIsForImport = newEntity instanceof ForImportEntity;

        if (existingIsMethod != newIsMethod) {
            return false;
        }
        if (existingIsForImport && newIsForImport) {
            // We don't want foo.Bar shadow foz.Bar if no class Bar is imported.
            return false;
        }
        if (!existingIsMethod) {
            return true;
        }

        // TODO: Implement method overriding detection.
        return false;
    }

    /**
     * A special entity that is considered not available in the current context and needs to be
     * imported.
     *
     * <p>This kind of entity will be shadowed by other entities, but not by each other.
     */
    public static class ForImportEntity extends Entity {
        private static Entity delegate;

        public ForImportEntity(Entity delegate) {
            super(
                    delegate.getSimpleName(),
                    delegate.getKind(),
                    delegate.getQualifiers(),
                    delegate.isStatic(),
                    delegate.getJavadoc(),
                    delegate.getSymbolRange());
            this.delegate = delegate;
        }

        @Override
        public EntityScope getScope() {
            return delegate.getScope();
        }

        @Override
        public Optional<EntityScope> getParentScope() {
            return delegate.getParentScope();
        }
    }
}

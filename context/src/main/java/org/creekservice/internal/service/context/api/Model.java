/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creekservice.internal.service.context.api;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.model.ModelContainer;
import org.creekservice.api.service.extension.model.ResourceHandler;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Model implements ModelContainer {

    private final long threadId;
    private final Map<Class<? extends ResourceDescriptor>, ResourceExtension<?>>
            resourceExtensions = new HashMap<>();
    private Optional<CreekExtensionProvider> currentProvider = Optional.empty();

    public Model() {
        this(Thread.currentThread().getId());
    }

    @VisibleForTesting
    Model(final long threadId) {
        this.threadId = threadId;
    }

    @Override
    public <T extends ResourceDescriptor> ModelContainer addResource(
            final Class<T> type, final ResourceHandler<? super T> handler) {
        throwIfNotOnCorrectThread();

        final ResourceExtension<?> existing = resourceExtensions.get(type);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Handler already registered for type: "
                            + type.getName()
                            + ", registering provider: "
                            + existing.provider);
        }

        resourceExtensions.put(
                type,
                new ResourceExtension<>(
                        handler, currentProvider.orElseThrow(NotWithinInitializeException::new)));
        return this;
    }

    @Override
    public boolean hasType(final Class<? extends ResourceDescriptor> type) {
        throwIfNotOnCorrectThread();

        return resourceExtension(type).isPresent();
    }

    public <T extends ResourceDescriptor> ResourceHandler<? super T> resourceHandler(
            final Class<T> resourceType) {
        throwIfNotOnCorrectThread();
        return resourceExtension(resourceType)
                .map(ext -> ext.handler)
                .orElseThrow(() -> new UnsupportedResourceTypesException(resourceType));
    }

    public void initializing(final Optional<CreekExtensionProvider> provider) {
        throwIfNotOnCorrectThread();

        currentProvider = requireNonNull(provider, "provider");
    }

    @SuppressWarnings("unchecked")
    private <T extends ResourceDescriptor> Optional<ResourceExtension<T>> resourceExtension(
            final Class<T> resourceType) {
        final ResourceExtension<?> ext = resourceExtensions.get(resourceType);
        if (ext != null) {
            return Optional.of((ResourceExtension<T>) ext);
        }

        final Map<Class<? extends ResourceDescriptor>, ? extends ResourceExtension<?>> found =
                resourceExtensions.entrySet().stream()
                        .filter(e -> e.getKey().isAssignableFrom(resourceType))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map<Class<? extends ResourceDescriptor>, ? extends ResourceExtension<?>> reduced =
                removeSuperTypes(found);

        switch (reduced.size()) {
            case 1:
                return Optional.of((ResourceExtension<T>) reduced.values().iterator().next());
            case 0:
                return Optional.empty();
            default:
                throw new IllegalArgumentException(
                        "Unable to determine most specific resource handler for type: "
                                + resourceType.getName()
                                + ". Could be any handler for any type in "
                                + reduced.entrySet().stream()
                                        .map(
                                                e ->
                                                        e.getKey().getSimpleName()
                                                                + " ("
                                                                + e.getValue().provider
                                                                + ")")
                                        .sorted()
                                        .collect(Collectors.joining(", ", "[", "]")));
        }
    }

    private Map<Class<? extends ResourceDescriptor>, ? extends ResourceExtension<?>>
            removeSuperTypes(
                    final Map<Class<? extends ResourceDescriptor>, ? extends ResourceExtension<?>>
                            types) {
        return types.entrySet().stream()
                .filter(
                        e ->
                                types.keySet().stream()
                                        .filter(t -> !t.equals(e.getKey()))
                                        .noneMatch(t -> e.getKey().isAssignableFrom(t)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private static final class ResourceExtension<T> {
        final ResourceHandler<? super T> handler;
        final CreekExtensionProvider provider;

        private ResourceExtension(
                final ResourceHandler<? super T> handler, final CreekExtensionProvider provider) {
            this.handler = requireNonNull(handler, "handler");
            this.provider = requireNonNull(provider, "provider");
        }
    }

    private static final class UnsupportedResourceTypesException extends RuntimeException {

        UnsupportedResourceTypesException(final Class<? extends ResourceDescriptor> type) {
            super(
                    "Unknown resource descriptor type: "
                            + type.getName()
                            + lineSeparator()
                            + "Are you missing a Creek extension on the class or module path?");
        }
    }

    private static final class NotWithinInitializeException extends UnsupportedOperationException {

        NotWithinInitializeException() {
            super(
                    "The model can only be changed during the CreekExtensionProvider.initialize call");
        }
    }
}

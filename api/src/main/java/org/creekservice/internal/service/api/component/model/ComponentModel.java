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

package org.creekservice.internal.service.api.component.model;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;
import static org.creekservice.api.base.type.CodeLocation.codeLocation;

import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ResourceHandler;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.component.model.ComponentModelContainer;
import org.creekservice.internal.service.api.extension.Extensions;
import org.creekservice.internal.service.api.util.SubTypeAwareMap;

/** Implementation of {@link ComponentModelContainer} */
public final class ComponentModel implements ComponentModelContainer {

    private final long threadId;
    private final Extensions extensions;
    private final SubTypeAwareMap<ResourceDescriptor, ResourceExtension<?>> resourceExtensions =
            new SubTypeAwareMap<>();

    /** @param extensions known extensions to Creek Service. */
    public ComponentModel(final Extensions extensions) {
        this(extensions, Thread.currentThread().getId());
    }

    @VisibleForTesting
    ComponentModel(final Extensions extensions, final long threadId) {
        this.threadId = threadId;
        this.extensions = requireNonNull(extensions, "extensions");
    }

    @Override
    public <T extends ResourceDescriptor> ComponentModelContainer addResource(
            final Class<T> type, final ResourceHandler<? super T> handler) {
        throwIfNotOnCorrectThread();

        final ResourceExtension<?> existing = resourceExtensions.get(type);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Handler already registered for type: "
                            + type.getName()
                            + ", registered by: "
                            + existing.provider
                            + "("
                            + codeLocation(existing.provider)
                            + ")");
        }

        resourceExtensions.put(
                type,
                new ResourceExtension<>(
                        handler,
                        extensions
                                .currentlyInitialising()
                                .orElseThrow(NotWithinInitializeException::new)));
        return this;
    }

    @Override
    public boolean hasType(final Class<? extends ResourceDescriptor> type) {
        throwIfNotOnCorrectThread();

        return resourceExtension(type).isPresent();
    }

    @Override
    public <T extends ResourceDescriptor> ResourceHandler<T> resourceHandler(
            final Class<T> resourceType) {
        throwIfNotOnCorrectThread();
        return resourceExtension(resourceType)
                .map(ext -> ext.handler)
                .orElseThrow(
                        () ->
                                new UnsupportedResourceTypesException(
                                        resourceType, resourceExtensions.keySet()));
    }

    @SuppressWarnings("unchecked")
    private <T extends ResourceDescriptor> Optional<ResourceExtension<T>> resourceExtension(
            final Class<T> resourceType) {
        try {
            return resourceExtensions.getOrSub(resourceType).map(e -> (ResourceExtension<T>) e);
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                    "Unable to determine most specific resource handler for type: "
                            + resourceType.getName(),
                    e);
        }
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private static final class ResourceExtension<T extends ResourceDescriptor> {
        final ResourceHandler<T> handler;
        final CreekExtensionProvider<?> provider;

        @SuppressWarnings("unchecked")
        private ResourceExtension(
                final ResourceHandler<? super T> handler,
                final CreekExtensionProvider<?> provider) {
            this.handler = (ResourceHandler<T>) requireNonNull(handler, "handler");
            this.provider = requireNonNull(provider, "provider");
        }
    }

    private static final class UnsupportedResourceTypesException extends RuntimeException {

        UnsupportedResourceTypesException(
                final Class<? extends ResourceDescriptor> type,
                final Set<Class<? extends ResourceDescriptor>> known) {
            super(
                    "Unknown resource descriptor type: "
                            + type.getName()
                            + lineSeparator()
                            + "Are you missing a Creek extension on the class or module path?"
                            + lineSeparator()
                            + "Known resource types: "
                            + format(known));
        }

        private static String format(final Set<Class<? extends ResourceDescriptor>> known) {
            return known.stream()
                    .map(t -> "\t" + t.getName() + " (" + codeLocation(t) + ")")
                    .collect(
                            Collectors.joining(
                                    "," + lineSeparator(),
                                    "[" + lineSeparator(),
                                    lineSeparator() + "]"));
        }
    }

    private static final class NotWithinInitializeException extends UnsupportedOperationException {

        NotWithinInitializeException() {
            super(
                    "The model can only be changed during the CreekExtensionProvider.initialize call");
        }
    }
}

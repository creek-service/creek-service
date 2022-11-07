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

package org.creekservice.internal.service.api.extension;

import static java.util.Objects.requireNonNull;
import static org.creekservice.api.base.type.CodeLocation.codeLocation;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.extension.ExtensionContainer;
import org.creekservice.internal.service.api.Creek;

/** Implementation of {@link ExtensionContainer} */
public final class Extensions implements ExtensionContainer {

    private final long threadId;
    private final Creek api;
    private final Stack<CreekExtensionProvider<?>> initStack = new Stack<>();

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends CreekExtensionProvider>, ExtensionData<?>> applied =
            new HashMap<>();

    private final Map<Class<? extends CreekExtension>, ExtensionData<?>> extensions =
            new LinkedHashMap<>();

    /** @param api the creek api */
    public Extensions(final Creek api) {
        this(api, Thread.currentThread().getId());
    }

    @VisibleForTesting
    Extensions(final Creek api, final long threadId) {
        this.threadId = threadId;
        this.api = requireNonNull(api, "api");
    }

    /** @return non-empty if an extension is currently being initialized. */
    public Optional<CreekExtensionProvider<?>> currentlyInitialising() {
        throwIfNotOnCorrectThread();
        return initStack.isEmpty() ? Optional.empty() : Optional.of(initStack.peek());
    }

    @Override
    public <T extends CreekExtension> T ensureExtension(
            final Class<? extends CreekExtensionProvider<T>> providerType) {
        return ensureExtension(createInstance(providerType));
    }

    /**
     * Ensure the extension {@code provider} has been applied, applying if necessary.
     *
     * @param provider the extension provider
     * @param <T> the type of the extension
     * @return the initialised extension
     */
    @SuppressWarnings("unchecked")
    public <T extends CreekExtension> T ensureExtension(final CreekExtensionProvider<T> provider) {
        throwIfNotOnCorrectThread();
        final ExtensionData<?> alreadyApplied = applied.get(provider.getClass());
        if (alreadyApplied != null) {
            return (T) alreadyApplied.extension;
        }

        final T ext = initialize(provider);

        final ExtensionData<T> data =
                new ExtensionData<>(
                        ext, (Class<? extends CreekExtensionProvider<T>>) provider.getClass());
        extensions.compute(
                ext.getClass(),
                (k, existing) -> {
                    if (existing != null) {
                        throw new ExtensionTypeClashException(ext, provider, existing.provider);
                    }
                    return data;
                });

        applied.put(provider.getClass(), data);
        return ext;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<CreekExtension> iterator() {
        throwIfNotOnCorrectThread();
        return extensions.values().stream().<CreekExtension>map(data -> data.extension).iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CreekExtension> T get(final Class<T> extensionType) {
        throwIfNotOnCorrectThread();
        return (T)
                extensions.values().stream()
                        .filter(data -> extensionType.isAssignableFrom(data.extension.getClass()))
                        .map(data -> data.extension)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new UnknownExtensionException(
                                                extensionType, extensions.values()));
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private <T extends CreekExtension> CreekExtensionProvider<T> createInstance(
            final Class<? extends CreekExtensionProvider<T>> providerType) {
        try {
            return providerType.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            throw new FailedToInstantiateProviderException(providerType, e);
        }
    }

    private <T extends CreekExtension> T initialize(final CreekExtensionProvider<T> provider) {
        try {
            initStack.push(provider);
            final T ext = provider.initialize(api);
            if (ext == null) {
                throw new NullPointerException(
                        "Provider returned null extension: "
                                + provider.getClass().getName()
                                + " ("
                                + codeLocation(provider)
                                + ")");
            }
            return ext;
        } finally {
            initStack.pop();
        }
    }

    private static final class ExtensionData<T extends CreekExtension> {
        private final T extension;
        private final Class<? extends CreekExtensionProvider<T>> provider;

        private ExtensionData(
                final T extension, final Class<? extends CreekExtensionProvider<T>> provider) {
            this.extension = requireNonNull(extension, "extension");
            this.provider = requireNonNull(provider, "provider");
        }
    }

    private static final class FailedToInstantiateProviderException extends RuntimeException {
        FailedToInstantiateProviderException(
                final Class<? extends CreekExtensionProvider<?>> providerType,
                final Throwable cause) {
            super(
                    "Failed to instantiate the extension provider. type: " + providerType.getName(),
                    cause);
        }
    }

    private static class ExtensionTypeClashException extends IllegalArgumentException {
        ExtensionTypeClashException(
                final CreekExtension type,
                final CreekExtensionProvider<?> currentProvider,
                final Class<? extends CreekExtensionProvider<?>> existingProvider) {
            super(
                    "Multiple extension providers returned the same extension type. This is not supported. "
                            + "extension_type: "
                            + type.getClass().getName()
                            + ", current_provider: "
                            + currentProvider.getClass().getName()
                            + " ("
                            + codeLocation(currentProvider)
                            + ") "
                            + ", existing_provider: "
                            + existingProvider.getName()
                            + " ("
                            + codeLocation(existingProvider)
                            + ") ");
        }
    }

    private static class UnknownExtensionException extends IllegalArgumentException {
        UnknownExtensionException(
                final Class<?> extensionType, final Collection<ExtensionData<?>> extensions) {
            super(
                    "No extension of requested type is registered: "
                            + extensionType
                            + ", installed_extensions: "
                            + extensions.stream()
                                    .map(data -> data.extension)
                                    .map(CreekExtension::name)
                                    .collect(Collectors.toList()));
        }
    }
}

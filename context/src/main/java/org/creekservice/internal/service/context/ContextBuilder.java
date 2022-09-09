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

package org.creekservice.internal.service.context;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.base.type.temporal.AccurateClock;
import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.observability.logging.structured.StructuredLogger;
import org.creekservice.api.observability.logging.structured.StructuredLoggerFactory;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.service.context.CreekContext;
import org.creekservice.api.service.context.CreekServices;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.internal.service.api.Creek;
import org.creekservice.internal.service.context.temporal.SystemEnvClockLoader;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ContextBuilder implements CreekServices.Builder {

    private static final StructuredLogger LOGGER =
            StructuredLoggerFactory.internalLogger(CreekServices.class);

    private final ComponentDescriptor component;
    private final ContextFactory contextFactory;
    private final UnhandledExceptionHandlerInstaller unhandledExceptionHandlerInstaller;
    private final Runnable systemExit;
    private final Creek api;
    private final List<CreekExtensionProvider> extensionProviders;
    private final ResourceInitializerFactory resourceInitializerFactory;
    private Optional<Clock> explicitClock = Optional.empty();

    public ContextBuilder(
            final ComponentDescriptor component,
            final Creek api,
            final List<CreekExtensionProvider> extensionProviders) {
        this(
                component,
                api,
                extensionProviders,
                ResourceInitializer::resourceInitializer,
                Context::new,
                Thread::setDefaultUncaughtExceptionHandler,
                () -> System.exit(-1));
    }

    @VisibleForTesting
    ContextBuilder(
            final ComponentDescriptor component,
            final Creek api,
            final List<CreekExtensionProvider> extensionProviders,
            final ResourceInitializerFactory resourceInitializerFactory,
            final ContextFactory contextFactory,
            final UnhandledExceptionHandlerInstaller unhandledExceptionHandlerInstaller,
            final Runnable systemExit) {
        this.api = requireNonNull(api, "api");
        this.extensionProviders =
                List.copyOf(requireNonNull(extensionProviders, "extensionProviders"));
        this.resourceInitializerFactory =
                requireNonNull(resourceInitializerFactory, "resourceInitializerFactory");
        this.contextFactory = requireNonNull(contextFactory, "contextFactory");
        this.unhandledExceptionHandlerInstaller =
                requireNonNull(
                        unhandledExceptionHandlerInstaller, "unhandledExceptionHandlerInstaller");
        this.systemExit = requireNonNull(systemExit, "systemExit");
        this.component = requireNonNull(component, "component");
    }

    @Override
    public CreekServices.Builder with(final Clock clock) {
        explicitClock = Optional.of(clock);
        return this;
    }

    @Override
    public ContextBuilder with(final CreekExtensionOptions options) {
        api.options().add(options);
        return this;
    }

    @Override
    public CreekContext build() {
        installDefaultUncaughtExceptionHandler();

        final Collection<CreekExtension> extensions = createExtensions();
        final CreekContext ctx = contextFactory.build(createClock(), extensions);
        throwOnUnsupportedResourceType(extensions);
        throwOnUnusedOptionType(extensions);

        resourceInitializer().service(List.of(component));

        return ctx;
    }

    private void installDefaultUncaughtExceptionHandler() {
        unhandledExceptionHandlerInstaller.install(
                (thread, throwable) -> {
                    LOGGER.error(
                            "uncaught exception on thread: terminating",
                            log ->
                                    log.with("thread-name", thread.getName())
                                            .withThrowable(throwable));
                    systemExit.run();
                });
    }

    private void throwOnUnsupportedResourceType(final Collection<CreekExtension> extensions) {
        final List<Object> unsupported =
                component
                        .resources()
                        .filter(resourceDef -> !api.model().hasType(resourceDef.getClass()))
                        .collect(Collectors.toList());

        if (!unsupported.isEmpty()) {
            throw new UnsupportedResourceTypesException(
                    component, installedExtensions(extensions), unsupported);
        }
    }

    private void throwOnUnusedOptionType(final Collection<CreekExtension> extensions) {
        final Set<CreekExtensionOptions> unused = api.options().unused();
        if (unused.isEmpty()) {
            return;
        }

        final String unusedText =
                unused.stream().map(Object::toString).collect(Collectors.joining(", "));

        throw new IllegalArgumentException(
                "No registered Creek extensions were interested in the following options: "
                        + unusedText
                        + ", "
                        + installedExtensions(extensions));
    }

    private Clock createClock() {
        return new SystemEnvClockLoader()
                .load(() -> explicitClock.orElseGet(AccurateClock::create));
    }

    private Collection<CreekExtension> createExtensions() {
        return extensionProviders.stream()
                .map(this::initialize)
                .collect(
                        Collectors.groupingBy(
                                CreekExtension::getClass,
                                collectingAndThen(
                                        Collectors.toList(),
                                        ContextBuilder::throwOnExtensionTypeClash)))
                .values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                CreekExtension::getClass, Comparator.comparing(Class::getName)))
                .collect(Collectors.toUnmodifiableList());
    }

    private CreekExtension initialize(final CreekExtensionProvider provider) {
        try {
            api.initializing(Optional.of(provider));
            return provider.initialize(api, List.of(component));
        } finally {
            api.initializing(Optional.empty());
        }
    }

    private ResourceInitializer resourceInitializer() {
        return resourceInitializerFactory.build(api.model()::resourceHandler);
    }

    private static CreekExtension throwOnExtensionTypeClash(final List<CreekExtension> extensions) {
        if (extensions.size() == 1) {
            return extensions.get(0);
        }

        throw new ExtensionTypeClashException(extensions);
    }

    private static String installedExtensions(final Collection<CreekExtension> extensions) {
        return extensions.stream()
                .map(CreekExtension::name)
                .collect(Collectors.joining(", ", "installed_extensions: ", ""));
    }

    private static class ExtensionTypeClashException extends IllegalArgumentException {
        ExtensionTypeClashException(final Collection<? extends CreekExtension> extensions) {
            super(
                    "Multiple extensions found with the same type. This is not supported. type: "
                            + type(extensions)
                            + ", locations: "
                            + locations(extensions));
        }

        private static String type(final Collection<? extends CreekExtension> extensions) {
            return extensions.iterator().next().getClass().getName();
        }

        private static String locations(final Collection<? extends CreekExtension> extensions) {
            return extensions.stream()
                    .map(Object::getClass)
                    .map(Class::getProtectionDomain)
                    .map(ProtectionDomain::getCodeSource)
                    .map(src -> src == null ? "unknown" : src.getLocation().toString())
                    .collect(Collectors.joining(", ", "[", "]"));
        }
    }

    @VisibleForTesting
    static final class UnsupportedResourceTypesException extends RuntimeException {

        UnsupportedResourceTypesException(
                final ComponentDescriptor component,
                final String installedExtensions,
                final List<Object> unsupportedResources) {
            super(
                    "Service descriptor defines resources for which no extension is installed. "
                            + "Are you missing a Creek extension on the class or module path? "
                            + "component: "
                            + component.name()
                            + ", unsupported_resources: "
                            + unsupportedResources
                            + ", "
                            + installedExtensions);
        }
    }

    @VisibleForTesting
    interface ContextFactory {
        CreekContext build(Clock clock, Collection<CreekExtension> extensions);
    }

    @VisibleForTesting
    interface ResourceInitializerFactory {
        ResourceInitializer build(ResourceInitializer.ResourceHandlers handlers);
    }

    @VisibleForTesting
    interface UnhandledExceptionHandlerInstaller {
        void install(UncaughtExceptionHandler handler);
    }
}

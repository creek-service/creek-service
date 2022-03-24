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

package org.creek.internal.service.context;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.creek.api.base.annotation.VisibleForTesting;
import org.creek.api.base.type.temporal.AccurateClock;
import org.creek.api.base.type.temporal.Clock;
import org.creek.api.observability.logging.structured.StructuredLogger;
import org.creek.api.observability.logging.structured.StructuredLoggerFactory;
import org.creek.api.platform.metadata.ComponentDescriptor;
import org.creek.api.service.context.CreekContext;
import org.creek.api.service.context.CreekServices;
import org.creek.api.service.extension.CreekExtension;
import org.creek.api.service.extension.CreekExtensionBuilder;
import org.creek.api.service.extension.CreekExtensionOptions;
import org.creek.api.service.extension.CreekExtensions;
import org.creek.internal.service.context.temporal.SystemEnvClockLoader;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ContextBuilder implements CreekServices.Builder {

    private static final StructuredLogger LOGGER =
            StructuredLoggerFactory.internalLogger(CreekServices.class);

    private final ComponentDescriptor component;
    private final ContextFactory contextFactory;
    private final UnhandledExceptionHandlerInstaller unhandledExceptionHandlerInstaller;
    private final Runnable systemExit;
    private final String installedExtensions;
    private final List<CreekExtensionBuilder> builders;
    private Optional<Clock> explicitClock = Optional.empty();

    public ContextBuilder(final ComponentDescriptor component) {
        this(
                component,
                CreekExtensions.load(),
                Context::new,
                Thread::setDefaultUncaughtExceptionHandler,
                () -> System.exit(-1));
    }

    @VisibleForTesting
    ContextBuilder(
            final ComponentDescriptor component,
            final List<CreekExtensionBuilder> builders,
            final ContextFactory contextFactory,
            final UnhandledExceptionHandlerInstaller unhandledExceptionHandlerInstaller,
            final Runnable systemExit) {
        this.builders = List.copyOf(requireNonNull(builders, "builders"));
        this.contextFactory = requireNonNull(contextFactory, "contextFactory");
        this.unhandledExceptionHandlerInstaller =
                requireNonNull(
                        unhandledExceptionHandlerInstaller, "unhandledExceptionHandlerInstaller");
        this.systemExit = requireNonNull(systemExit, "systemExit");
        this.installedExtensions =
                builders.stream()
                        .map(CreekExtensionBuilder::name)
                        .collect(Collectors.joining(", ", "installed_extensions: ", ""));
        this.component = requireNonNull(component, "component");

        throwOnUnsupportedResourceType();
    }

    @Override
    public CreekServices.Builder with(final Clock clock) {
        explicitClock = Optional.of(clock);
        return this;
    }

    @Override
    public ContextBuilder with(final CreekExtensionOptions options) {
        final boolean handled =
                builders.stream()
                        .map(builder -> builder.with(options))
                        .reduce((b0, b1) -> b0 || b1)
                        .orElse(false);

        if (!handled) {
            throw new IllegalArgumentException(
                    "No registered extensions support the supplied options: "
                            + options
                            + ", "
                            + installedExtensions);
        }

        return this;
    }

    @Override
    public CreekContext build() {
        installDefaultUncaughtExceptionHandler();

        return contextFactory.build(createClock(), createExtensions());
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

    private void throwOnUnsupportedResourceType() {
        final List<Object> unsupported =
                component
                        .resources()
                        .filter(
                                resourceDef ->
                                        builders.stream()
                                                .noneMatch(ext -> ext.handles(resourceDef)))
                        .collect(Collectors.toList());

        if (!unsupported.isEmpty()) {
            throw new UnsupportedResourceTypesException(
                    component, installedExtensions, unsupported);
        }
    }

    private Clock createClock() {
        return new SystemEnvClockLoader()
                .load(() -> explicitClock.orElseGet(AccurateClock::create));
    }

    private Collection<CreekExtension> createExtensions() {
        return builders.stream()
                .map(ext -> ext.build(component))
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

    private static CreekExtension throwOnExtensionTypeClash(final List<CreekExtension> extensions) {
        if (extensions.size() == 1) {
            return extensions.get(0);
        }

        throw new ExtensionTypeClashException(extensions);
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
                    "Component defines resources for which no extension is installed. "
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
    interface UnhandledExceptionHandlerInstaller {
        void install(UncaughtExceptionHandler handler);
    }
}

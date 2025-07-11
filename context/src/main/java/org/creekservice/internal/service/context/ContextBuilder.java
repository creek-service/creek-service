/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.base.type.temporal.AccurateClock;
import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.observability.logging.structured.StructuredLogger;
import org.creekservice.api.observability.logging.structured.StructuredLoggerFactory;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.CreatableResource;
import org.creekservice.api.platform.metadata.ResourceCollection;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.service.context.CreekContext;
import org.creekservice.api.service.context.CreekServices;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.component.model.ResourceHandler;
import org.creekservice.internal.service.api.Creek;
import org.creekservice.internal.service.api.extension.Extensions;
import org.creekservice.internal.service.context.temporal.SystemEnvClockLoader;

/** Implementation of {@link CreekServices.Builder} */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ContextBuilder implements CreekServices.Builder {

    private static final StructuredLogger LOGGER =
            StructuredLoggerFactory.internalLogger(CreekServices.class);

    private final ComponentDescriptor component;
    private final ContextFactory contextFactory;
    private final UnhandledExceptionHandlerInstaller unhandledExceptionHandlerInstaller;
    private final Runnable systemExit;
    private final Creek api;
    private final List<CreekExtensionProvider<?>> extensionProviders;
    private final ResourceInitializerFactory resourceInitializerFactory;
    private Optional<Clock> explicitClock = Optional.empty();

    /**
     * @param component the component to build a context for
     * @param api the creek api
     * @param extensionProviders all known extension providers
     */
    public ContextBuilder(
            final ComponentDescriptor component,
            final Creek api,
            final List<CreekExtensionProvider<?>> extensionProviders) {
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
            final List<CreekExtensionProvider<?>> extensionProviders,
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

        initializeExtensions();
        throwOnUnsupportedResourceType();
        throwOnUnusedOptionType();

        resourceInitializer().service(List.of(component));

        prepareExtensions();

        return contextFactory.build(createClock(), api.extensions());
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
                ResourceCollection.collectResources(component)
                        .filter(
                                resourceDef ->
                                        !api.components().model().hasType(resourceDef.getClass()))
                        .collect(Collectors.toList());

        if (!unsupported.isEmpty()) {
            throw new UnsupportedResourceTypesException(
                    component, installedExtensions(), unsupported);
        }
    }

    private void throwOnUnusedOptionType() {
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
                        + installedExtensions());
    }

    private Clock createClock() {
        return new SystemEnvClockLoader()
                .load(() -> explicitClock.orElseGet(AccurateClock::create));
    }

    private void initializeExtensions() {
        final Extensions extensions = api.extensions();
        extensionProviders.forEach(extensions::ensureExtension);
    }

    private ResourceInitializer resourceInitializer() {
        return resourceInitializerFactory.build(
                new ResourceInitializer.Callbacks() {
                    @Override
                    public <T extends ResourceDescriptor> void validate(
                            final Class<T> type, final Collection<T> resourceGroup) {
                        api.components().model().resourceHandler(type).validate(resourceGroup);
                    }

                    @Override
                    public <T extends CreatableResource> void ensure(
                            final Class<T> type, final Collection<T> creatableResources) {
                        api.components().model().resourceHandler(type).ensure(creatableResources);
                    }
                });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void prepareExtensions() {
        final Map<URI, ResourceDescriptor> uniqueById =
                ResourceCollection.collectResources(component)
                        .collect(
                                groupingBy(
                                        ResourceDescriptor::id,
                                        LinkedHashMap::new,
                                        collectingAndThen(toList(), l -> l.get(0))));

        final Map<Class<? extends ResourceDescriptor>, List<ResourceDescriptor>> byType =
                uniqueById.values().stream()
                        .collect(
                                groupingBy(
                                        ResourceDescriptor::getClass,
                                        LinkedHashMap::new,
                                        toList()));

        byType.forEach(
                (type, resources) -> {
                    final ResourceHandler handler = api.components().model().resourceHandler(type);
                    handler.prepare(resources);
                });
    }

    private String installedExtensions() {
        return api.extensions().stream()
                .map(CreekExtension::name)
                .collect(Collectors.joining(", ", "installed_extensions: ", ""));
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
        CreekContext build(Clock clock, Extensions extensions);
    }

    @VisibleForTesting
    interface ResourceInitializerFactory {
        ResourceInitializer build(ResourceInitializer.Callbacks callbacks);
    }

    @VisibleForTesting
    interface UnhandledExceptionHandlerInstaller {
        void install(UncaughtExceptionHandler handler);
    }
}

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

import static org.creekservice.internal.service.context.ContextBuilder.UnsupportedResourceTypesException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.creekservice.api.base.type.temporal.AccurateClock;
import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.platform.resource.ResourceInitializer.ResourceHandlers;
import org.creekservice.api.service.context.CreekContext;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.internal.service.context.ContextBuilder.ContextFactory;
import org.creekservice.internal.service.context.ContextBuilder.UnhandledExceptionHandlerInstaller;
import org.creekservice.internal.service.context.api.Creek;
import org.creekservice.internal.service.context.api.Model;
import org.creekservice.internal.service.context.api.Options;
import org.creekservice.internal.service.context.temporal.SystemEnvClockLoader;
import org.creekservice.internal.service.context.temporal.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextBuilderTest {

    @Mock private Creek api;
    @Mock private Creek initializingApi;
    @Mock private Options options;
    @Mock private Model model;
    @Mock private ComponentDescriptor component;
    @Mock private ResourceA res0;
    @Mock private ResourceB res1;
    @Mock private CreekExtensionOptions customOptions;
    @Mock private CreekExtensionProvider extProvider0;
    @Mock private CreekExtensionProvider extProvider1;
    @Mock private TestExtensionA ext0;
    @Mock private TestExtensionB ext1;
    @Mock private ContextFactory contextFactory;
    @Mock private CreekContext ctx;
    @Mock private Runnable systemExit;
    @Mock private UnhandledExceptionHandlerInstaller exceptionHandlerInstaller;
    @Mock private Clock specificClock;
    @Mock private ContextBuilder.ResourceInitializerFactory resourceInitializerFactory;
    @Mock private ResourceInitializer resourceInitializer;
    @Captor private ArgumentCaptor<UncaughtExceptionHandler> exceptionHandlerCaptor;
    private ContextBuilder ctxBuilder;

    @BeforeEach
    void setUp() {
        when(api.options()).thenReturn(options);
        when(api.model()).thenReturn(model);
        when(api.initializing(any())).thenReturn(initializingApi);

        when(model.hasType(any())).thenReturn(true);

        when(component.name()).thenReturn("comp");
        when(component.resources()).thenAnswer(inv -> Stream.of(res0, res1));
        when(contextFactory.build(any(), any())).thenReturn(ctx);

        when(extProvider0.initialize(any())).thenReturn(ext0);
        when(ext0.name()).thenReturn("provider0");

        when(extProvider1.initialize(any())).thenReturn(ext1);
        when(ext1.name()).thenReturn("provider1");

        when(resourceInitializerFactory.build(any())).thenReturn(resourceInitializer);

        ctxBuilder = newContextBuilder();
    }

    @Test
    void shouldThrowOnUnhandledOptions() {
        // Given:
        when(options.unused()).thenReturn(Set.of(customOptions));

        // When:
        final Exception e = assertThrows(IllegalArgumentException.class, ctxBuilder::build);

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "No registered Creek extensions were interested in the following options: customOptions, "
                                + "installed_extensions: provider0, provider1"));
    }

    @Test
    void shouldAddOptions() {
        // Given:
        ctxBuilder.with(customOptions);

        // When:
        ctxBuilder.build();

        // Then: did not throw.
        verify(options).add(customOptions);
    }

    @Test
    void shouldThrowOnUnknownResourceType() {
        // Given:
        when(model.hasType(res0.getClass())).thenReturn(false);
        when(model.hasType(res1.getClass())).thenReturn(true);

        // When:
        final Exception e =
                assertThrows(UnsupportedResourceTypesException.class, ctxBuilder::build);

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Service descriptor defines resources for which no extension is installed. "
                                + "Are you missing a Creek extension on the class or module path? "
                                + "component: comp, unsupported_resources: [res0], installed_extensions: provider0, provider1"));
    }

    @Test
    void shouldIncludeAllUnsupportedResourcesInException() {
        // Given:
        when(model.hasType(res0.getClass())).thenReturn(false);
        when(model.hasType(res1.getClass())).thenReturn(false);

        // When:
        final Exception e =
                assertThrows(UnsupportedResourceTypesException.class, ctxBuilder::build);

        // Then:
        assertThat(e.getMessage(), containsString("unsupported_resources: [res0, res1]"));
    }

    @Test
    void shouldPassApiToExtensionProviders() {
        // When:
        ctxBuilder.build();

        // Then:
        verify(extProvider0).initialize(initializingApi);
        verify(extProvider1).initialize(initializingApi);
    }

    @Test
    void shouldPassInitializingExtensionProvidersToApi() {
        // When:
        ctxBuilder.build();

        // Then:
        final InOrder inOrder = Mockito.inOrder(api);
        inOrder.verify(api).initializing(Optional.of(extProvider0));
        inOrder.verify(api).initializing(Optional.empty());
        inOrder.verify(api).initializing(Optional.of(extProvider1));
        inOrder.verify(api).initializing(Optional.empty());
    }

    @Test
    void shouldBuildContextWithSortedExtensions() {
        // Given:
        final CreekContext result = ctxBuilder.build();

        // Then:
        verify(contextFactory).build(any(), eq(List.of(ext0, ext1)));
        assertThat(result, is(ctx));
    }

    @Test
    void shouldInstallDefaultExceptionHandlerThatSystemExists() {
        // Given:
        verify(exceptionHandlerInstaller, never()).install(any());

        // When:
        ctxBuilder.build();

        // Then:
        verify(exceptionHandlerInstaller).install(exceptionHandlerCaptor.capture());
        final UncaughtExceptionHandler handler = exceptionHandlerCaptor.getValue();

        // When:
        handler.uncaughtException(Thread.currentThread(), new RuntimeException());

        // Then:
        verify(systemExit).run();
    }

    @Test
    void shouldProvideDefaultClockImpl() {
        // When:
        ctxBuilder.build();

        // Then:
        verify(contextFactory).build(isA(AccurateClock.class), any());
    }

    @Test
    void shouldUseSpecificClockImpl() {
        // Given:
        ctxBuilder.with(specificClock);

        // When:
        ctxBuilder.build();

        // Then:
        verify(contextFactory).build(eq(specificClock), any());
    }

    @SetEnvironmentVariable(
            key = SystemEnvClockLoader.ENV_VAR_NAME,
            value = "org.creekservice.internal.service.context.temporal.TestClock")
    @Test
    void shouldOverrideClockImpl() {
        // Given:
        ctxBuilder.with(specificClock);

        // When:
        ctxBuilder.build();

        // Then:
        verify(contextFactory).build(isA(TestClock.class), any());
    }

    @Test
    void shouldThrowHelpfulExceptionOnMultipleImplsOfSameExtension() {
        // Given:
        when(extProvider1.initialize(any())).thenReturn(ext0);

        // When:
        final Exception e = assertThrows(RuntimeException.class, ctxBuilder::build);

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Multiple extensions found with the same type. This is not supported. "));
        assertThat(e.getMessage(), containsString("type: " + ext0.getClass().getName()));
        assertThat(
                e.getMessage(),
                containsString(
                        "locations: ["
                                + getClass().getProtectionDomain().getCodeSource().getLocation()));
    }

    @Test
    void shouldCreateResourceInitializerWithCorrectParams() {
        // Given:
        ctxBuilder.build();

        final ArgumentCaptor<ResourceHandlers> captor =
                ArgumentCaptor.forClass(ResourceHandlers.class);
        verify(resourceInitializerFactory).build(captor.capture());
        final ResourceHandlers handlers = captor.getValue();

        // When:
        handlers.get(ResourceA.class);

        // Then:
        verify(model).resourceHandler(ResourceA.class);
    }

    @Test
    void shouldInitializeResources() {
        // When:
        ctxBuilder.build();

        // Then:
        verify(resourceInitializer).service(List.of(component));
    }

    @Test
    void shouldThrowIfResourceInitializationFails() {
        // Given:
        final RuntimeException expected = new RuntimeException("boom");
        doThrow(expected).when(resourceInitializer).service(any());

        // When:
        final Exception e = assertThrows(RuntimeException.class, ctxBuilder::build);

        // Then:
        assertThat(e, is(sameInstance(expected)));
    }

    private ContextBuilder newContextBuilder() {
        return new ContextBuilder(
                component,
                api,
                List.of(extProvider0, extProvider1),
                resourceInitializerFactory,
                contextFactory,
                exceptionHandlerInstaller,
                systemExit);
    }

    private interface TestExtensionA extends CreekExtension {}

    private interface TestExtensionB extends CreekExtension {}

    private interface ResourceA extends ResourceDescriptor {}

    private interface ResourceB extends ResourceDescriptor {}
}

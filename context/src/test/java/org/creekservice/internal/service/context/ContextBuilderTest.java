/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.creekservice.api.base.type.temporal.AccurateClock;
import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.OwnedResource;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.service.context.CreekContext;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.component.model.ResourceHandler;
import org.creekservice.internal.service.api.Creek;
import org.creekservice.internal.service.api.component.model.ComponentModel;
import org.creekservice.internal.service.api.extension.Extensions;
import org.creekservice.internal.service.api.options.Options;
import org.creekservice.internal.service.context.ContextBuilder.ContextFactory;
import org.creekservice.internal.service.context.ContextBuilder.UnhandledExceptionHandlerInstaller;
import org.creekservice.internal.service.context.temporal.SystemEnvClockLoader;
import org.creekservice.internal.service.context.temporal.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.Isolated;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Isolated // This test uses @SetEnvironmentVariable, which modifies global env
@Execution(SAME_THREAD) // ...this isn't thread-safe. So isolate from other tests.
class ContextBuilderTest {

    private static final URI RES0_ID = URI.create("res://0");
    private static final URI RES1_ID = URI.create("res://1");

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Creek api;

    @Mock private Options options;
    @Mock private ComponentModel model;
    @Mock private ComponentDescriptor component;
    @Mock private ResourceA res0;
    @Mock private ResourceB res1;
    @Mock private CreekExtensionOptions customOptions;
    @Mock private CreekExtensionProvider<TestExtensionA> extProvider0;
    @Mock private CreekExtensionProvider<TestExtensionB> extProvider1;
    @Mock private TestExtensionA ext0;
    @Mock private TestExtensionB ext1;
    @Mock private ContextFactory contextFactory;
    @Mock private CreekContext ctx;
    @Mock private Runnable systemExit;
    @Mock private UnhandledExceptionHandlerInstaller exceptionHandlerInstaller;
    @Mock private Clock specificClock;
    @Mock private ContextBuilder.ResourceInitializerFactory resourceInitializerFactory;
    @Mock private ResourceInitializer resourceInitializer;
    @Mock private ResourceHandler<ResourceDescriptor> resourceHandler;
    @Captor private ArgumentCaptor<UncaughtExceptionHandler> exceptionHandlerCaptor;
    private ContextBuilder ctxBuilder;

    @BeforeEach
    void setUp() {
        when(api.options()).thenReturn(options);
        when(api.components().model()).thenReturn(model);
        when(api.extensions().stream()).thenReturn(Stream.of(ext0, ext1));

        when(model.hasType(any())).thenReturn(true);
        when(model.resourceHandler(any())).thenReturn(resourceHandler);

        when(component.name()).thenReturn("comp");
        when(component.resources()).thenAnswer(inv -> Stream.of(res0));
        when(res0.resources()).thenAnswer(inv -> Stream.of(res1));
        when(contextFactory.build(any(), any())).thenReturn(ctx);

        when(extProvider0.initialize(any())).thenReturn(ext0);
        when(ext0.name()).thenReturn("provider0");

        when(extProvider1.initialize(any())).thenReturn(ext1);
        when(ext1.name()).thenReturn("provider1");

        when(resourceInitializerFactory.build(any())).thenReturn(resourceInitializer);

        when(res0.id()).thenReturn(RES0_ID);
        when(res1.id()).thenReturn(RES1_ID);

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
                        "No registered Creek extensions were interested in the following options:"
                                + " customOptions, installed_extensions: provider0, provider1"));
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
                        "Service descriptor defines resources for which no extension is installed."
                                + " Are you missing a Creek extension on the class or module path?"
                                + " component: comp, unsupported_resources: [res0],"
                                + " installed_extensions: provider0, provider1"));
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
        assertThat(e.getMessage(), containsString("unsupported_resources: [res1, res0]"));
    }

    @Test
    void shouldInitializeProviders() {
        // When:
        ctxBuilder.build();

        // Then:
        verify(api.extensions()).ensureExtension(extProvider0);
        verify(api.extensions()).ensureExtension(extProvider1);
    }

    @Test
    void shouldBuildContextWithExtensions() {
        // Given:
        final CreekContext result = ctxBuilder.build();

        // Then:
        final Extensions extensions = api.extensions();
        verify(contextFactory).build(any(), eq(extensions));
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

    @SuppressWarnings("unchecked")
    @Test
    void shouldValidateResourcesOnCallback() {
        // Given:
        ctxBuilder.build();
        final ResourceInitializer.Callbacks callbacks = captureCallbacks();

        // When:
        callbacks.validate((Class<ResourceA>) res0.getClass(), List.of(res0));

        // Then:
        verify(model).resourceHandler(res0.getClass());
        verify(resourceHandler).validate(List.of(res0));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldEnsureResourcesOnCallback() {
        // Given:
        ctxBuilder.build();
        final ResourceInitializer.Callbacks callbacks = captureCallbacks();

        // When:
        callbacks.ensure((Class<ResourceA>) res0.getClass(), List.of(res0));

        // Then:
        verify(model).resourceHandler(res0.getClass());
        verify(resourceHandler).ensure(List.of(res0));
    }

    @Test
    void shouldInitializeServiceResources() {
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

    @Test
    void shouldPrepareNestedResourcesFirst() {
        // When:
        ctxBuilder.build();

        // Then:
        final InOrder inOrder = Mockito.inOrder(resourceHandler);
        inOrder.verify(resourceHandler).prepare(List.of(res1));
        inOrder.verify(resourceHandler).prepare(List.of(res0));
    }

    @Test
    void shouldPrepareResourcesOncePerId() {
        // Given:
        when(res1.id()).thenReturn(RES0_ID);

        // When:
        ctxBuilder.build();

        // Then:
        verify(resourceHandler).prepare(List.of(res1));
        verify(resourceHandler, never()).prepare(List.of(res0));
    }

    @Test
    void shouldGroupPrepareResourcesByResourceType() {
        // Given:
        final ResourceA res2 = mock(ResourceA.class);
        when(res2.id()).thenReturn(RES1_ID);
        when(component.resources()).thenAnswer(inv -> Stream.of(res0, res2));
        when(res0.resources()).thenAnswer(inv -> Stream.of());

        // When:
        ctxBuilder.build();

        // Then:
        verify(resourceHandler).prepare(List.of(res0, res2));
    }

    @Test
    void shouldThrowIfResourcePreparationFails() {
        // Given:
        final RuntimeException expected = new RuntimeException("boom");
        doThrow(expected).when(resourceHandler).prepare(any());

        // When:
        final Exception e = assertThrows(RuntimeException.class, ctxBuilder::build);

        // Then:
        assertThat(e, is(expected));
    }

    private ResourceInitializer.Callbacks captureCallbacks() {
        final ArgumentCaptor<ResourceInitializer.Callbacks> captor =
                ArgumentCaptor.forClass(ResourceInitializer.Callbacks.class);
        verify(resourceInitializerFactory).build(captor.capture());
        final ResourceInitializer.Callbacks callbacks = captor.getValue();
        clearInvocations(model);
        return callbacks;
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

    private interface ResourceA extends ResourceDescriptor, OwnedResource {}

    private interface ResourceB extends ResourceDescriptor {}
}

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.stream.Stream;
import org.creekservice.api.base.type.temporal.AccurateClock;
import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.service.context.CreekContext;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionBuilder;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.internal.service.context.ContextBuilder.ContextFactory;
import org.creekservice.internal.service.context.ContextBuilder.UnhandledExceptionHandlerInstaller;
import org.creekservice.internal.service.context.ContextBuilder.UnsupportedResourceTypesException;
import org.creekservice.internal.service.context.temporal.SystemEnvClockLoader;
import org.creekservice.internal.service.context.temporal.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextBuilderTest {

    @Mock private ComponentDescriptor component;
    @Mock private ResourceDescriptor res0;
    @Mock private ResourceDescriptor res1;
    @Mock private CreekExtensionOptions options;
    @Mock private CreekExtensionBuilder extBuilder0;
    @Mock private CreekExtensionBuilder extBuilder1;
    @Mock private TestExtensionA ext0;
    @Mock private TestExtensionB ext1;
    @Mock private ContextFactory contextFactory;
    @Mock private CreekContext ctx;
    @Mock private Runnable systemExit;
    @Mock private UnhandledExceptionHandlerInstaller exceptionHandlerInstaller;
    @Mock private Clock specificClock;
    @Captor private ArgumentCaptor<UncaughtExceptionHandler> exceptionHandlerCaptor;
    private ContextBuilder ctxBuilder;

    @BeforeEach
    void setUp() {
        when(component.name()).thenReturn("comp");
        when(extBuilder0.name()).thenReturn("builder0");
        when(extBuilder0.handles(res0)).thenReturn(true);
        when(extBuilder0.with(any())).thenReturn(true);
        when(extBuilder0.build(any())).thenReturn(ext0);
        when(extBuilder1.name()).thenReturn("builder1");
        when(extBuilder1.handles(res1)).thenReturn(true);
        when(extBuilder1.with(any())).thenReturn(true);
        when(extBuilder1.build(any())).thenReturn(ext1);
        when(component.resources()).thenAnswer(inv -> Stream.of(res0, res1));
        when(contextFactory.build(any(), any())).thenReturn(ctx);

        ctxBuilder = newContextBuilder();
    }

    @Test
    void shouldThrowIfOptionsNotHandledByAnyExtension() {
        // Given:
        when(extBuilder0.with(any())).thenReturn(false);
        when(extBuilder1.with(any())).thenReturn(false);

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> ctxBuilder.with(options));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "No registered extensions support the supplied options: options, installed_extensions: builder0, builder1"));
    }

    @Test
    void shouldNotThrowIfAtLeastOneExtensionHandlesOptions() {
        // Given:
        when(extBuilder0.with(any())).thenReturn(false);
        when(extBuilder1.with(any())).thenReturn(true);

        // When:
        ctxBuilder.with(options);

        // Then: did not throw.
    }

    @Test
    void shouldNotThrowIfMoreThanExtensionHandlesOptions() {
        // Given:
        when(extBuilder0.with(any())).thenReturn(true);
        when(extBuilder1.with(any())).thenReturn(true);

        // When:
        ctxBuilder.with(options);

        // Then: did not throw.
    }

    @Test
    void shouldPassOptionsToAllBuilders() {
        // When:
        ctxBuilder.with(options);

        // Then:
        verify(extBuilder0).with(options);
        verify(extBuilder0).with(options);
    }

    @Test
    void shouldThrowIfResourceNotHandledByAnyExtension() {
        // Given:
        when(extBuilder0.handles(any())).thenReturn(false);

        // When:
        final Exception e =
                assertThrows(UnsupportedResourceTypesException.class, this::newContextBuilder);

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Component defines resources for which no extension is installed. "
                                + "Are you missing a Creek extension on the class or module path? "
                                + "component: comp, unsupported_resources: [res0], installed_extensions: builder0, builder1"));
    }

    @Test
    void shouldIncludeAllUnsupportedResourcesInException() {
        // Given:
        when(extBuilder0.handles(any())).thenReturn(false);
        when(extBuilder1.handles(any())).thenReturn(false);

        // When:
        final Exception e =
                assertThrows(UnsupportedResourceTypesException.class, this::newContextBuilder);

        // Then:
        assertThat(e.getMessage(), containsString("unsupported_resources: [res0, res1]"));
    }

    @Test
    void shouldPassComponentToBuilders() {
        // When:
        ctxBuilder.build();

        // Then:
        verify(extBuilder0).build(component);
        verify(extBuilder1).build(component);
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
        when(extBuilder0.build(any())).thenReturn(ext0);
        when(extBuilder1.build(any())).thenReturn(ext0);

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

    private ContextBuilder newContextBuilder() {
        return new ContextBuilder(
                component,
                List.of(extBuilder0, extBuilder1),
                contextFactory,
                exceptionHandlerInstaller,
                systemExit);
    }

    private interface TestExtensionA extends CreekExtension {}

    private interface TestExtensionB extends CreekExtension {}
}

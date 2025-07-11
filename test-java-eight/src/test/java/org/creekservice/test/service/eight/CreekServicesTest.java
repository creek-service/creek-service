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

package org.creekservice.test.service.eight;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.base.type.temporal.AccurateClock;
import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.platform.metadata.ComponentInput;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.context.CreekContext;
import org.creekservice.api.service.context.CreekServices;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.test.api.java.nine.service.extension.JavaNineExtension;
import org.creekservice.test.api.java.nine.service.extension.JavaNineExtensionOptions;
import org.creekservice.test.api.java.nine.service.extension.JavaNineExtensionProvider2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreekServicesTest {

    @Mock private ServiceDescriptor serviceDescriptor;
    private final JavaNineExtensionProvider2.Input java9Input =
            new JavaNineExtensionProvider2.Input();
    private final JavaNineExtensionProvider2.Internal java9Internal =
            new JavaNineExtensionProvider2.Internal();
    private final JavaNineExtensionProvider2.Output java9Output =
            new JavaNineExtensionProvider2.Output();

    @BeforeEach
    void setUp() {
        when(serviceDescriptor.name()).thenReturn("the-service");
        when(serviceDescriptor.dockerImage()).thenReturn("the-image");
        when(serviceDescriptor.resources()).thenCallRealMethod();
    }

    @Test
    void shouldExposeDefaultClock() {
        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        assertThat(ctx.clock(), is(instanceOf(AccurateClock.class)));
    }

    @Test
    void shouldExposeSpecificClock() {
        // Given:
        final Clock clock = mock(Clock.class);
        // When:
        final CreekContext ctx = CreekServices.builder(serviceDescriptor).with(clock).build();

        // Then:
        assertThat(ctx.clock(), is(sameInstance(clock)));
    }

    @Test
    void shouldExposeExtensions() {
        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        final JavaNineExtensionProvider2.Extension ext =
                ctx.extension(JavaNineExtensionProvider2.Extension.class);
        assertThat(ext.components(), is(List.of(serviceDescriptor)));
    }

    @Test
    void shouldValidateAndEnsureOwnedResourcesBeforePrepare() {
        // Given
        when(serviceDescriptor.outputs()).thenReturn(List.of(java9Output));

        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        final LinkedHashMap<String, Object> executionOrder =
                ctx.extension(JavaNineExtensionProvider2.Extension.class).executionOrder();

        final List<String> outputOrder =
                executionOrder.keySet().stream()
                        .filter(k -> k.startsWith("output ") || k.startsWith("inner "))
                        .collect(Collectors.toList());

        // Prepare doesn't have guaranteed order:
        assertThat(
                outputOrder,
                is(
                        List.of(
                                "inner validate",
                                "output validate",
                                "inner ensure",
                                "output ensure",
                                "inner prepare",
                                "output prepare")));
    }

    @Test
    void shouldValidateUnownedResourcesBeforePrepare() {
        // Given
        when(serviceDescriptor.inputs()).thenReturn(List.of(java9Input));

        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        final LinkedHashMap<String, Object> executionOrder =
                ctx.extension(JavaNineExtensionProvider2.Extension.class).executionOrder();

        final List<String> outputOrder =
                executionOrder.keySet().stream()
                        .filter(k -> k.startsWith("input "))
                        .collect(Collectors.toList());

        assertThat(outputOrder, is(List.of("input validate", "input prepare")));
    }

    @Test
    void shouldValidateUnmanagedResourcesBeforePrepare() {
        // Given
        when(serviceDescriptor.internals()).thenReturn(List.of(java9Internal));

        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        final LinkedHashMap<String, Object> executionOrder =
                ctx.extension(JavaNineExtensionProvider2.Extension.class).executionOrder();

        final List<String> outputOrder =
                executionOrder.keySet().stream()
                        .filter(k -> k.startsWith("internal "))
                        .collect(Collectors.toList());

        assertThat(outputOrder, is(List.of("internal validate", "internal prepare")));
    }

    @Test
    void shouldExposeExtensionsWithPrivateImpl() {
        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        assertThat(ctx.extension(JavaNineExtension.class), is(instanceOf(JavaNineExtension.class)));
    }

    @Test
    void shouldThrowIfOptionsNotHandledByAnyExtension() {
        // Given:
        final CreekServices.Builder builder =
                CreekServices.builder(serviceDescriptor).with(new UnhandledExtensionOptions());

        // Then:
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldNotThrowIfOptionsHandled() {
        // Given:
        final CreekServices.Builder builder = CreekServices.builder(serviceDescriptor);

        // Then: does not throw.
        builder.with(new JavaNineExtensionOptions()).build();
    }

    @Test
    void shouldThrowOnUnhandledResource() {
        // Given:
        when(serviceDescriptor.inputs()).thenReturn(List.of(new UnhandledResourceDef()));

        // Then:
        final Exception e =
                assertThrows(Exception.class, () -> CreekServices.context(serviceDescriptor));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Service descriptor defines resources for which no extension is"
                                + " installed."));
    }

    @Test
    void shouldNotThrowIfAllResourcesHandled() {
        // Given:
        when(serviceDescriptor.inputs()).thenReturn(List.of(java9Input));
        when(serviceDescriptor.internals()).thenReturn(List.of(java9Internal));
        when(serviceDescriptor.outputs()).thenReturn(List.of(java9Output));

        // When:
        final CreekContext ctx = CreekServices.context(serviceDescriptor);

        // Then:
        assertThat(ctx, is(notNullValue()));
    }

    private static final class UnhandledExtensionOptions implements CreekExtensionOptions {}

    private static final class UnhandledResourceDef implements ComponentInput {
        @Override
        public URI id() {
            return URI.create("kafka-topic://cluster/topic");
        }
    }
}

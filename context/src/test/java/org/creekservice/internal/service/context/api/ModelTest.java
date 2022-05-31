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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.model.ResourceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelTest {

    private Model model;
    @Mock private CreekExtensionProvider provider;
    @Mock private ResourceHandler<? super BaseResource> handler;

    @BeforeEach
    void setUp() {
        model = new Model();
        model.initializing(Optional.of(provider));
    }

    @Test
    void shouldThrowIfAddingResourceOutsideOfInitializeCall() {
        // Given:
        model.initializing(Optional.empty());

        // When:
        final Exception e =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> model.addResource(BaseResource.class, handler));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "The model can only be changed during the CreekExtensionProvider.initialize call"));
    }

    @Test
    void shouldAddResource() {
        // When:
        model.addResource(BaseResource.class, handler);

        // Then:
        assertThat(model.hasType(BaseResource.class), is(true));
    }

    @Test
    void shouldReturnTrueFromHaveTypeIfSuperTypeRegistered() {
        // When:
        model.addResource(BaseResource.class, handler);

        // Then:
        assertThat(model.hasType(TestResource.class), is(true));
    }

    @Test
    void shouldThrowDuplicateResource() {
        // Given:
        model.addResource(BaseResource.class, handler);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> model.addResource(BaseResource.class, handler));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Handler already registered for type: "
                                + "org.creekservice.internal.service.context.api.ModelTest$BaseResource, "
                                + "registering provider: provider"));
    }

    @Test
    void shouldGetResourceHandlerForExactType() {
        // Given:
        model.addResource(BaseResource.class, handler);

        // When:
        final ResourceHandler<? super BaseResource> result =
                model.resourceHandler(BaseResource.class);

        // Then:
        assertThat(result, is(handler));
    }

    @Test
    void shouldGetResourceHandlerForSubType() {
        // Given:
        model.addResource(BaseResource.class, handler);

        // When:
        final ResourceHandler<? super TestResource> result =
                model.resourceHandler(TestResource.class);

        // Then:
        assertThat(result, is(handler));
    }

    @Test
    void shouldThrowIfResourceHandlerNotRegistered() {
        // When:
        final Exception e =
                assertThrows(
                        RuntimeException.class, () -> model.resourceHandler(TestResource.class));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Unknown resource descriptor type: "
                                + "org.creekservice.internal.service.context.api.ModelTest$TestResource"
                                + System.lineSeparator()
                                + "Are you missing a Creek extension on the class or module path?"));
    }

    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<Model> method) {
        // Given:
        model = new Model(Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(model));
    }

    @Test
    void shouldHaveThreadingTestForEachPublicMethod() {
        final int publicMethodCount = publicMethodCount();
        final int testedMethodCount = (int) publicMethods().count();
        assertThat(testedMethodCount, is(publicMethodCount));
    }

    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of(
                        "addResource",
                        (Consumer<Model>)
                                m -> m.addResource(TestResource.class, new ResourceHandler<>() {})),
                Arguments.of("hasType", (Consumer<Model>) m -> m.hasType(TestResource.class)),
                Arguments.of(
                        "resourceHandler",
                        (Consumer<Model>) m -> m.resourceHandler(TestResource.class)),
                Arguments.of(
                        "initializing", (Consumer<Model>) m -> m.initializing(Optional.empty())));
    }

    private int publicMethodCount() {
        return (int)
                Arrays.stream(Model.class.getMethods())
                        .filter(m -> !m.getDeclaringClass().equals(Object.class))
                        .count();
    }

    private interface BaseResource extends ResourceDescriptor {}

    private static final class TestResource implements BaseResource {}
}

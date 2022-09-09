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

package org.creekservice.internal.service.api.model;

import static java.lang.System.lineSeparator;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ResourceHandler;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComponentModelTest {

    private ComponentModel model;
    @Mock private CreekExtensionProvider provider;
    @Mock private ResourceHandler<? super ResourceDescriptor> handler1;
    @Mock private ResourceHandler<? super ResourceDescriptor> handler2;
    @Mock private ResourceHandler<? super ResourceDescriptor> handler3;

    @BeforeEach
    void setUp() {
        model = new ComponentModel();
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
                        () -> model.addResource(BaseResource.class, handler1));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "The model can only be changed during the CreekExtensionProvider.initialize call"));
    }

    @Test
    void shouldAddResource() {
        // When:
        model.addResource(BaseResource.class, handler1);

        // Then:
        assertThat(model.hasType(BaseResource.class), is(true));
    }

    @Test
    void shouldReturnTrueFromHaveTypeIfSuperTypeRegistered() {
        // When:
        model.addResource(BaseResource.class, handler1);

        // Then:
        assertThat(model.hasType(TestResource.class), is(true));
    }

    @Test
    void shouldAllowAddOfSubType() {
        // Given:
        model.addResource(BaseResource.class, handler1);

        // When:
        model.addResource(TestResource.class, handler2);

        // Then:
        assertThat(model.resourceHandler(BaseResource.class), is(handler1));
        assertThat(model.resourceHandler(TestResource.class), is(handler2));
    }

    @Test
    void shouldAllowAddOfSuperType() {
        // Given:
        model.addResource(TestResource.class, handler1);

        // When:
        model.addResource(BaseResource.class, handler2);

        // Then:
        assertThat(model.resourceHandler(TestResource.class), is(handler1));
        assertThat(model.resourceHandler(BaseResource.class), is(handler2));
    }

    @Test
    void shouldReturnMostSpecificHandler() {
        // Given:
        model.addResource(ResourceDescriptor.class, handler1);
        model.addResource(TestResource.class, handler3);
        model.addResource(BaseResource.class, handler2);

        // Then:
        assertThat(model.resourceHandler(TestResource2.class), is(handler3));
        assertThat(model.resourceHandler(TestResource3.class), is(handler3));
    }

    @Test
    void shouldThrowIfMostSpecificHandlerIsAmbiguous() {
        // Given:
        model.addResource(ResourceDescriptor.class, handler1);
        model.addResource(BaseResource.class, handler2);
        model.addResource(BaseResource2.class, handler3);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> model.resourceHandler(TestResource4.class));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Unable to determine most specific resource handler for type: "
                                + "org.creekservice.internal.service.api.model.ComponentModelTest$TestResource4. "
                                + "Could be any handler for any type in [BaseResource (provider), BaseResource2 (provider)]"));
    }

    @Test
    void shouldThrowOnAddOnDuplicateResource() {
        // Given:
        model.addResource(BaseResource.class, handler1);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> model.addResource(BaseResource.class, handler1));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Handler already registered for type: "
                                + "org.creekservice.internal.service.api.model.ComponentModelTest$BaseResource, "
                                + "registered by: provider"));
    }

    @Test
    void shouldGetResourceHandlerForExactType() {
        // Given:
        model.addResource(BaseResource.class, handler1);

        // When:
        final ResourceHandler<? super BaseResource> result =
                model.resourceHandler(BaseResource.class);

        // Then:
        assertThat(result, is(handler1));
    }

    @Test
    void shouldGetResourceHandlerForSubType() {
        // Given:
        model.addResource(BaseResource.class, handler1);

        // When:
        final ResourceHandler<? super TestResource> result =
                model.resourceHandler(TestResource.class);

        // Then:
        assertThat(result, is(handler1));
    }

    @Test
    void shouldThrowIfResourceHandlerNotRegistered() {
        // Given:
        model.addResource(TestResource2.class, handler1);
        model.addResource(TestResource3.class, handler1);

        // When:
        final Exception e =
                assertThrows(
                        RuntimeException.class, () -> model.resourceHandler(TestResource.class));

        // Then:
        assertThat(
                e.getMessage(),
                matchesRegex(
                        compile(
                                quote(
                                                "Unknown resource descriptor type: "
                                                        + TestResource.class.getName()
                                                        + lineSeparator()
                                                        + "Are you missing a Creek extension on the class or module path?"
                                                        + lineSeparator()
                                                        + "Known resource types: ["
                                                        + lineSeparator())
                                        + ".*"
                                        + quote(lineSeparator() + "]"),
                                Pattern.DOTALL)));

        assertThat(
                e.getMessage(),
                matchesRegex(
                        compile(
                                ".*"
                                        + quote(
                                                lineSeparator()
                                                        + "\t"
                                                        + TestResource2.class.getName())
                                        + " \\(file:/[^)]*\\).*",
                                Pattern.DOTALL)));

        assertThat(
                e.getMessage(),
                matchesRegex(
                        compile(
                                ".*"
                                        + quote(
                                                lineSeparator()
                                                        + "\t"
                                                        + TestResource3.class.getName())
                                        + " \\(file:/[^)]*\\).*",
                                Pattern.DOTALL)));
    }

    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<ComponentModel> method) {
        // Given:
        model = new ComponentModel(Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(model));
    }

    @Test
    void shouldHaveThreadingTestForEachPublicMethod() {
        final int publicMethodCount = publicMethodCount();
        final int testedMethodCount = (int) publicMethods().count();
        assertThat(testedMethodCount, is(publicMethodCount));
    }

    @SuppressWarnings("unchecked")
    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of(
                        "addResource",
                        (Consumer<ComponentModel>)
                                m ->
                                        m.addResource(
                                                TestResource.class, mock(ResourceHandler.class))),
                Arguments.of(
                        "hasType", (Consumer<ComponentModel>) m -> m.hasType(TestResource.class)),
                Arguments.of(
                        "resourceHandler",
                        (Consumer<ComponentModel>) m -> m.resourceHandler(TestResource.class)),
                Arguments.of(
                        "initializing",
                        (Consumer<ComponentModel>) m -> m.initializing(Optional.empty())));
    }

    private int publicMethodCount() {
        return (int)
                Arrays.stream(ComponentModel.class.getMethods())
                        .filter(m -> !m.getDeclaringClass().equals(Object.class))
                        .count();
    }

    private interface BaseResource extends ResourceDescriptor {}

    private interface BaseResource2 extends ResourceDescriptor {}

    private static class TestResource implements BaseResource {

        @Override
        public URI id() {
            return null;
        }
    }

    /** ResourceDescriptor - BaseResource - TestResource - TestResource2 */
    private static class TestResource2 extends TestResource {}

    /**
     * /- BaseResource - TestResource -\ ResourceDescriptor -| |- TestResource3 \--------
     * BaseResource ---------\
     */
    private static class TestResource3 extends TestResource implements BaseResource {}

    /** /- BaseResource -\ ResourceDescriptor -| |- TestResource4 \- BaseResource2 -\ */
    private static class TestResource4 implements BaseResource, BaseResource2 {

        @Override
        public URI id() {
            return null;
        }
    }
}

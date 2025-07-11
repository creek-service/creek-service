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

package org.creekservice.internal.service.api.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OptionsTest {

    private Options options;
    private final TestOptionsA userOptionsA = new TestOptionsA();
    private final TestOptionsB userOptionsB = new TestOptionsB();

    @BeforeEach
    void setUp() {
        options = new Options();
    }

    @Test
    void shouldAddOption() {
        // When:
        options.add(userOptionsA);

        // Then:
        assertThat(options.get(TestOptionsA.class), is(Optional.of(userOptionsA)));
    }

    @Test
    void shouldThrowOnDuplicateOptions() {
        // Given:
        options.add(userOptionsA);

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> options.add(userOptionsA));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Option of supplied type is already registered. type:"
                            + " org.creekservice.internal.service.api.options.OptionsTest$TestOptionsA"));
    }

    @Test
    void shouldReturnUsed() {
        // Given:
        options.add(userOptionsA);
        options.add(userOptionsB);
        options.get(userOptionsA.getClass());

        // When:
        final Set<CreekExtensionOptions> result = options.unused();

        // Then:
        assertThat(result, is(Set.of(userOptionsB)));
    }

    @Test
    void shouldGetByExactType() {
        // Given:
        options.add(userOptionsA);
        options.add(userOptionsB);

        // Then:
        assertThat(options.get(TestOptionsA.class), is(Optional.of(userOptionsA)));
        assertThat(options.get(TestOptionsB.class), is(Optional.of(userOptionsB)));
    }

    @Test
    void shouldGetBySubType() {
        // Given:
        options.add(userOptionsA);

        // Then:
        assertThat(options.get(BaseOptions.class), is(Optional.of(userOptionsA)));
    }

    @Test
    void shouldRemoveUnusedBySubType() {
        // Given:
        options.add(userOptionsA);
        options.get(BaseOptions.class);

        // Then:
        assertThat(options.unused(), is(empty()));
    }

    @Test
    void shouldThrowOnAmbiguousGet() {
        // Given:
        options.add(userOptionsA);
        options.add(userOptionsB);

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> options.get(BaseOptions.class));

        // Return:
        assertThat(
                e.getMessage(),
                is(
                        "Requested option type is ambiguous: "
                            + "org.creekservice.internal.service.api.options.OptionsTest$BaseOptions"));

        assertThat(e.getCause().getMessage(), startsWith("Ambiguous entry"));
    }

    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<Options> method) {
        // Given:
        options = new Options(Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(options));
    }

    @Test
    void shouldHaveThreadingTestForEachPublicMethod() {
        final List<String> publicMethodNames = publicMethodNames();
        final List<String> tested = testedMethodNames();
        assertThat(
                "Public methods:\n"
                        + String.join(System.lineSeparator(), publicMethodNames)
                        + "\nTested methods:\n"
                        + String.join(System.lineSeparator(), tested),
                tested,
                hasSize(publicMethodNames.size()));
    }

    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of("add", (Consumer<Options>) o -> o.add(new TestOptionsA())),
                Arguments.of("get", (Consumer<Options>) o -> o.get(TestOptionsA.class)),
                Arguments.of("unused", (Consumer<Options>) Options::unused));
    }

    private static List<String> testedMethodNames() {
        return publicMethods()
                .map(a -> (String) a.get()[0])
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(Options.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }

    private interface BaseOptions extends CreekExtensionOptions {}

    private static final class TestOptionsA implements BaseOptions {}

    private static final class TestOptionsB implements BaseOptions {}
}

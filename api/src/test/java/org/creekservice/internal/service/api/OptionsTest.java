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

package org.creekservice.internal.service.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
                        "Option of supplied type is already registered. "
                                + "type: org.creekservice.internal.service.api.OptionsTest$TestOptionsA"));
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
        final int publicMethodCount = publicMethodCount();
        final int testedMethodCount = (int) publicMethods().count();
        assertThat(testedMethodCount, is(publicMethodCount));
    }

    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of("add", (Consumer<Options>) o -> o.add(new TestOptionsA())),
                Arguments.of("get", (Consumer<Options>) o -> o.get(TestOptionsA.class)),
                Arguments.of("unused", (Consumer<Options>) Options::unused));
    }

    private int publicMethodCount() {
        return (int)
                Arrays.stream(Options.class.getMethods())
                        .filter(m -> !m.getDeclaringClass().equals(Object.class))
                        .count();
    }

    private static final class TestOptionsA implements CreekExtensionOptions {}

    private static final class TestOptionsB implements CreekExtensionOptions {}
}

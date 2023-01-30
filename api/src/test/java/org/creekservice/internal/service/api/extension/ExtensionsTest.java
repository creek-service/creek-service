/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.service.api.extension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockito.quality.Strictness.LENIENT;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.internal.service.api.Creek;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtensionsTest {

    @Mock private Creek api;
    private Extensions extensions;

    @BeforeEach
    void setUp() {
        extensions = new Extensions(api);
    }

    @Test
    void shouldEnsureByInstance() {
        // Given:
        final TestExtension ext = mock(TestExtension.class);

        // When:
        final TestExtension result = extensions.ensureExtension(new TestExtensionProvider(ext));

        // Then:
        assertThat(result, is(sameInstance(ext)));
    }

    @Test
    void shouldEnsureByType() {
        // When:
        final TestExtension result = extensions.ensureExtension(TestExtensionProvider.class);

        // Then:
        assertThat(result, is(notNullValue()));
    }

    @Test
    void shouldThrowIfCanNotCreateInstance() {
        // When:
        final Exception e =
                assertThrows(
                        RuntimeException.class,
                        () -> extensions.ensureExtension(UncreatableProvider.class));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Failed to instantiate the extension provider. type: "
                                + UncreatableProvider.class.getName()));
        assertThat(e.getCause(), is(instanceOf(NoSuchMethodException.class)));
    }

    @Test
    void shouldReturnExistingFromEnsure() {
        // Given:
        final TestExtension previous = extensions.ensureExtension(TestExtensionProvider.class);

        // When:
        final TestExtension result = extensions.ensureExtension(TestExtensionProvider.class);

        // Then:
        assertThat(result, is(sameInstance(previous)));
    }

    @Test
    void shouldThrowIfTwoProvidersExposeSameExtensionType() {
        // Given:
        final PrivateExtensionImpl impl = new PrivateExtensionImpl();
        extensions.ensureExtension(new TestExtensionProvider(impl));

        // When:
        final Exception e =
                assertThrows(
                        RuntimeException.class,
                        () -> extensions.ensureExtension(new DuplicateExtensionProvider(impl)));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Multiple extension providers returned the same extension type. "
                                + "This is not supported. "
                                + "extension_type: "
                                + PrivateExtensionImpl.class.getName()
                                + ", "
                                + "current_provider: "
                                + DuplicateExtensionProvider.class.getName()));
        assertThat(
                e.getMessage(),
                containsString(
                        "existing_provider: org.creekservice.internal.service.api.extension.ExtensionsTest$TestExtensionProvider"));
    }

    @Test
    void shouldStartWithEmptyStack() {
        assertThat(extensions.currentlyInitialising(), is(Optional.empty()));
    }

    @Test
    void shouldPushAndPopInitialisingProviders() {
        // Given:
        final TestExtensionProvider p0 = mock(TestExtensionProvider.class);
        final DiffExtensionProvider p1 = mock(DiffExtensionProvider.class);

        final TestExtension e0 = mock(TestExtension.class);
        final DiffExtension e1 = mock(DiffExtension.class);

        when(p0.initialize(any()))
                .thenAnswer(
                        inv -> {
                            assertThat(extensions.currentlyInitialising(), is(Optional.of(p0)));
                            extensions.ensureExtension(p1);
                            assertThat(extensions.currentlyInitialising(), is(Optional.of(p0)));
                            return e0;
                        });

        when(p1.initialize(any()))
                .thenAnswer(
                        inv -> {
                            assertThat(extensions.currentlyInitialising(), is(Optional.of(p1)));
                            return e1;
                        });

        // When:
        extensions.ensureExtension(p0);

        // Then:
        assertThat(extensions.currentlyInitialising(), is(Optional.empty()));
    }

    @Test
    void shouldPopInitialisingProviderOnException() {
        // Given:
        final TestExtensionProvider p0 = mock(TestExtensionProvider.class);
        final RuntimeException expected = new IllegalArgumentException("boom");
        when(p0.initialize(any())).thenThrow(expected);

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> extensions.ensureExtension(p0));

        // Then:
        assertThat(extensions.currentlyInitialising(), is(Optional.empty()));
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldIterate() {
        // Given:
        extensions.ensureExtension(TestExtensionProvider.class);
        extensions.ensureExtension(DiffExtensionProvider.class);

        // When:
        final Iterator<CreekExtension> it = extensions.iterator();

        // Then:
        final List<CreekExtension> items = new ArrayList<>();
        it.forEachRemaining(items::add);
        assertThat(
                items, contains(instanceOf(TestExtension.class), instanceOf(DiffExtension.class)));
    }

    @Test
    void shouldThrowOnUnknownExtension() {
        // Given:
        extensions.ensureExtension(TestExtensionProvider.class);

        // When:
        final Exception e =
                assertThrows(Exception.class, () -> extensions.get(UnknownExtension.class));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "No extension of requested type is registered: "
                                + UnknownExtension.class
                                + ", installed_extensions: [org.creekservice.test]"));
    }

    @Test
    void shouldGetExtensionByType() {
        // Given:
        extensions.ensureExtension(TestExtensionProvider.class);
        extensions.ensureExtension(DiffExtensionProvider.class);

        // Then:
        assertThat(extensions.get(TestExtension.class), is(instanceOf(TestExtension.class)));
        assertThat(extensions.get(DiffExtension.class), is(instanceOf(DiffExtension.class)));
    }

    @Test
    void shouldGetExtensionBySubType() {
        // Given:
        extensions.ensureExtension(TestExtensionProvider.class);

        // Then:
        assertThat(extensions.get(BaseExtension.class), is(instanceOf(BaseExtension.class)));
    }

    @Test
    void shouldGetFirstExtensionThatMatches() {
        // Given:
        extensions.ensureExtension(TestExtensionProvider.class);
        extensions.ensureExtension(DiffExtensionProvider.class);

        // Then:
        assertThat(extensions.get(BaseExtension.class), is(instanceOf(TestExtension.class)));
    }

    @Test
    void shouldThrowIfProviderReturnsNull() {
        // Given:
        final TestExtensionProvider p0 = mock(TestExtensionProvider.class);
        when(p0.initialize(any())).thenReturn(null);

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> extensions.ensureExtension(p0));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Provider returned null extension: "
                                + TestExtensionProvider.class.getName()));
    }

    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<Extensions> method) {
        // Given:
        extensions = new Extensions(api, Thread.currentThread().getId() + 1);

        // Then:
        Assertions.assertThrows(
                ConcurrentModificationException.class, () -> method.accept(extensions));
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

    @SuppressWarnings("unchecked")
    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of(
                        "currentlyInitialising",
                        (Consumer<Extensions>) Extensions::currentlyInitialising),
                Arguments.of(
                        "ensureExtension(Class)",
                        (Consumer<Extensions>) m -> m.ensureExtension(TestExtensionProvider.class)),
                Arguments.of(
                        "ensureExtension(Provider)",
                        (Consumer<Extensions>) m -> m.ensureExtension(new TestExtensionProvider())),
                Arguments.of("iterator", (Consumer<Extensions>) Extensions::iterator),
                Arguments.of("spliterator", (Consumer<Extensions>) Extensions::spliterator),
                Arguments.of("stream", (Consumer<Extensions>) Extensions::stream),
                Arguments.of(
                        "forEach", (Consumer<Extensions>) m -> m.forEach(mock(Consumer.class))),
                Arguments.of("get", (Consumer<Extensions>) m -> m.get(TestExtension.class)));
    }

    private static List<String> testedMethodNames() {
        return publicMethods()
                .map(a -> (String) a.get()[0])
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(Extensions.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }

    private interface BaseExtension extends CreekExtension {}

    private interface TestExtension extends BaseExtension {}

    public static final class TestExtensionProvider
            implements CreekExtensionProvider<TestExtension> {
        private final TestExtension ext;

        @SuppressWarnings("unused") // reflection
        TestExtensionProvider() {
            this(createMock());
        }

        TestExtensionProvider(final TestExtension ext) {
            this.ext = ext;
        }

        @Override
        public TestExtension initialize(final CreekService api) {
            return ext == null ? mock(TestExtension.class) : ext;
        }

        private static TestExtension createMock() {
            final TestExtension mock =
                    mock(TestExtension.class, withSettings().strictness(LENIENT));
            when(mock.name()).thenReturn("org.creekservice.test");
            return mock;
        }
    }

    public static final class DuplicateExtensionProvider
            implements CreekExtensionProvider<TestExtension> {

        private final TestExtension ext;

        @SuppressWarnings("unused") // reflection
        DuplicateExtensionProvider() {
            this(null);
        }

        DuplicateExtensionProvider(final TestExtension ext) {
            this.ext = ext;
        }

        @Override
        public TestExtension initialize(final CreekService api) {
            return ext == null ? new PrivateExtensionImpl() : ext;
        }
    }

    private interface DiffExtension extends BaseExtension {}

    public static final class DiffExtensionProvider
            implements CreekExtensionProvider<DiffExtension> {

        @Override
        public DiffExtension initialize(final CreekService api) {
            return mock(DiffExtension.class);
        }
    }

    public interface UncreatableProvider extends CreekExtensionProvider<TestExtension> {}

    private static class PrivateExtensionImpl implements TestExtension {
        @Override
        public String name() {
            return "test";
        }
    }

    private interface UnknownExtension extends CreekExtension {}
}

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

package org.creek.internal.service.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.creek.api.service.extension.CreekExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextTest {

    @Mock private TestExtension0 ext0;
    @Mock private TestExtension1 ext1;
    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context(List.of(ext0, ext1));

        when(ext0.name()).thenReturn("ext0");
        when(ext1.name()).thenReturn("ext1");
    }

    @Test
    void shouldGetExtensionByType() {
        assertThat(ctx.extension(ext0.getClass()), is(ext0));
        assertThat(ctx.extension(ext1.getClass()), is(ext1));
    }

    @Test
    void shouldThrowMeaningfulExceptionIfTwoExtensionsHaveTheSameType() {
        // When:
        final Exception e = assertThrows(Exception.class, () -> new Context(List.of(ext0, ext0)));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Multiple extensions found with the same type. This is not supported. type: "
                                + ext0.getClass()));
    }

    @Test
    void shouldThrowExceptionOnGetOfUnregisteredExtensionType() {
        // When:
        final Exception e =
                assertThrows(Exception.class, () -> ctx.extension(CreekExtension.class));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "No extension of requested type is registered: " + CreekExtension.class));
        assertThat(
                e.getMessage(),
                either(containsString(", installed_extensions: [ext0, ext1]"))
                        .or(containsString(", installed_extensions: [ext1, ext0]")));
    }

    private interface TestExtension0 extends CreekExtension {}

    private interface TestExtension1 extends CreekExtension {}
}

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
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.creek.api.service.extension.CreekExtension;
import org.creekservice.api.base.type.temporal.Clock;
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

    @Mock private Clock clock;
    @Mock private TestExtension0 ext0;
    @Mock private PrivateExtensionImpl ext1;
    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context(clock, List.of(ext0, ext1));

        when(ext0.name()).thenReturn("ext0");
        when(ext1.name()).thenReturn("ext1");
    }

    @Test
    void shouldExposeClock() {
        assertThat(ctx.clock(), is(sameInstance(clock)));
    }

    @Test
    void shouldGetExtensionByType() {
        assertThat(ctx.extension(TestExtension0.class), is(ext0));
    }

    @Test
    void shouldGetExtensionBySubType() {
        assertThat(ctx.extension(PublicExtensionInterface.class), is(ext1));
    }

    @Test
    void shouldGetFirstExtensionThatMatches() {
        assertThat(ctx.extension(CreekExtension.class), is(ext0));
    }

    @Test
    void shouldThrowExceptionOnGetOfUnregisteredExtensionType() {
        // When:
        final Exception e =
                assertThrows(Exception.class, () -> ctx.extension(UnknownExtension.class));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "No extension of requested type is registered: " + UnknownExtension.class));
        assertThat(
                e.getMessage(),
                either(containsString(", installed_extensions: [ext0, ext1]"))
                        .or(containsString(", installed_extensions: [ext1, ext0]")));
    }

    private interface TestExtension0 extends CreekExtension {}

    private interface PublicExtensionInterface extends CreekExtension {}

    private abstract static class PrivateExtensionImpl implements PublicExtensionInterface {}

    private interface UnknownExtension extends CreekExtension {}
}

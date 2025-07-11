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

package org.creekservice.internal.service.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.internal.service.api.extension.Extensions;
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
    @Mock private TestExtension ext;
    @Mock private Extensions extensions;
    private Context ctx;

    @BeforeEach
    void setUp() {
        ctx = new Context(clock, extensions);
    }

    @Test
    void shouldExposeClock() {
        assertThat(ctx.clock(), is(sameInstance(clock)));
    }

    @Test
    void shouldGetExtensionByType() {
        // Given:
        when(extensions.get(TestExtension.class)).thenReturn(ext);

        // When:
        final TestExtension result = ctx.extension(TestExtension.class);

        // Then:
        assertThat(result, is(ext));
    }

    @Test
    void shouldCloseExtensionsOnClose() {
        // When:
        ctx.close();

        // Then:
        verify(extensions).close();
    }

    private interface TestExtension extends CreekExtension {}
}

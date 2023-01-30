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

package org.creekservice.internal.service.context.temporal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.creekservice.api.base.type.temporal.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemEnvClockLoaderTest {

    @Mock private Clock defaultClock;
    private SystemEnvClockLoader loader;

    @BeforeEach
    void setUp() {
        loader = new SystemEnvClockLoader();
    }

    @Test
    void shouldDefaultToSuppliedDefaultClock() {
        assertThat(loader.load(() -> defaultClock), is(sameInstance(defaultClock)));
    }

    @SetEnvironmentVariable(
            key = SystemEnvClockLoader.ENV_VAR_NAME,
            value = "org.creekservice.internal.service.context.temporal.TestClock")
    @Test
    void shouldOverrideDefaultIfEnvVariableSet() {
        assertThat(loader.load(() -> defaultClock), is(instanceOf(TestClock.class)));
    }

    @SetEnvironmentVariable(
            key = SystemEnvClockLoader.ENV_VAR_NAME,
            value = "org.creekservice.api.service.context.temporal.UnknownClass")
    @Test
    void shouldThrowIfCanNotCreateClock() {
        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> loader.load(() -> defaultClock));

        // Then:
        assertThat(e.getMessage(), startsWith("Failed to create instance"));
    }

    @SetEnvironmentVariable(key = SystemEnvClockLoader.ENV_VAR_NAME, value = "java.lang.String")
    @Test
    void shouldThrowIfEnvVariableNotClockSubtype() {
        assertThrows(ClassCastException.class, () -> loader.load(() -> defaultClock));
    }
}

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

package org.creekservice.api.service.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.junit.jupiter.api.Test;

class CreekServicesTest {

    @Test
    void shouldValidateComponentDescriptor() {
        // Given:
        final ServiceDescriptor descriptor = new BadDescriptor();

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> CreekServices.context(descriptor));

        // Then:
        assertThat(e.getClass().getSimpleName(), is("InvalidDescriptorException"));
    }

    private static final class BadDescriptor implements ServiceDescriptor {
        @Override
        public String name() {
            return null;
        }

        @Override
        public String dockerImage() {
            return null;
        }
    }
}

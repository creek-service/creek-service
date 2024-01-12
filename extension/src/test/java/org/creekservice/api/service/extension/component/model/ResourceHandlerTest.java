/*
 * Copyright 2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.service.extension.component.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.junit.jupiter.api.Test;

class ResourceHandlerTest {

    @Test
    void shouldThrowUnsupportedFromEnsureByDefault() {
        // Given:
        final TestResourceHandler handler = new TestResourceHandler();

        // When:
        final Exception e =
                assertThrows(UnsupportedOperationException.class, () -> handler.ensure(List.of()));

        // Then:
        assertThat(e.getMessage(), is("Not a handler of owned resources"));
    }

    private interface TestResource extends ResourceDescriptor {}

    private static final class TestResourceHandler implements ResourceHandler<TestResource> {

        @Override
        public void validate(final Collection<? extends TestResource> resourceGroup) {}

        @Override
        public void prepare(final Collection<? extends TestResource> resources) {}
    }
}

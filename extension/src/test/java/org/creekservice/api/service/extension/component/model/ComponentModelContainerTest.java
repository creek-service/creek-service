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

import com.google.common.testing.EqualsTester;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.service.extension.component.model.ComponentModelContainer.HandlerTypeRef;
import org.junit.jupiter.api.Test;

class ComponentModelContainerTest {

    @Test
    void shouldImplementHashCodeAndEquals() {

        new EqualsTester()
                .addEqualityGroup(
                        new HandlerTypeRef<TestDescriptor<?>>() {},
                        new HandlerTypeRef<TestDescriptor<?>>() {},
                        new HandlerTypeRef<TestDescriptor<String>>() {})
                .addEqualityGroup(new HandlerTypeRef<DiffDescriptor<?>>() {})
                .testEquals();
    }

    @Test
    void shouldImplementMeaningfulToString() {
        // Given:
        final HandlerTypeRef<TestDescriptor<?>> typeRef = new HandlerTypeRef<>() {};

        // Then:
        assertThat(
                typeRef.toString(),
                is(
                        "HandlerTypeRef<"
                            + "org.creekservice.api.service.extension.component.model.ComponentModelContainerTest"
                            + "$TestDescriptor>(){}"));
    }

    @SuppressWarnings("unused")
    private interface TestDescriptor<T> extends ResourceDescriptor {}

    @SuppressWarnings("unused")
    private interface DiffDescriptor<T> extends ResourceDescriptor {}
}

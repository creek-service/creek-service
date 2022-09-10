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

import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Optional;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.internal.service.api.component.model.ComponentModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreekTest {

    @Mock private ComponentModel model;
    @Mock private Options options;
    @Mock private Collection<? extends ComponentDescriptor> components;
    @Mock private CreekExtensionProvider<?> provider;
    private Creek api;

    @BeforeEach
    void setUp() {
        api = new Creek(options, model, components);
    }

    @Test
    void shouldPassInitializingProviderToModel() {
        // When:
        api.initializing(Optional.of(provider));

        // Then:
        verify(model).initializing(Optional.of(provider));
    }
}

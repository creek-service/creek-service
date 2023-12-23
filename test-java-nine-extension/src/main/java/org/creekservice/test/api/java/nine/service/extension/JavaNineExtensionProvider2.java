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

package org.creekservice.test.api.java.nine.service.extension;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentInput;
import org.creekservice.api.platform.metadata.ComponentInternal;
import org.creekservice.api.platform.metadata.ComponentOutput;
import org.creekservice.api.platform.metadata.OwnedResource;
import org.creekservice.api.platform.metadata.UnownedResource;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.api.service.extension.component.model.ResourceHandler;

public final class JavaNineExtensionProvider2
        implements CreekExtensionProvider<JavaNineExtensionProvider2.Extension> {

    public JavaNineExtensionProvider2() {}

    @Override
    public Extension initialize(final CreekService api) {
        final Extension ext =
                new Extension(api.components().descriptors().stream().collect(Collectors.toList()));

        api.components()
                .model()
                .addResource(Input.class, ext.inputHandler)
                .addResource(Internal.class, ext.internalHandler)
                .addResource(Output.class, ext.outputHandler);

        return ext;
    }

    public static final class Extension implements CreekExtension {

        private static final String NAME = "java9_2";

        private final Collection<? extends ComponentDescriptor> components;
        private final InputHandler inputHandler = new InputHandler();
        private final InternalHandler internalHandler = new InternalHandler();
        private final OutputHandler outputHandler = new OutputHandler();
        private final LinkedHashMap<String, Object> executionOrder = new LinkedHashMap<>();

        public Extension(final Collection<? extends ComponentDescriptor> components) {
            this.components = requireNonNull(components, "components");
        }

        @Override
        public String name() {
            return NAME;
        }

        public Collection<? extends ComponentDescriptor> components() {
            return List.copyOf(components);
        }

        public LinkedHashMap<String, Object> executionOrder() {
            return new LinkedHashMap<>(executionOrder);
        }

        private final class InputHandler implements ResourceHandler<Input> {

            @Override
            public void validate(final Collection<? extends Input> resources) {
                executionOrder.put("input validate", resources);
            }

            @Override
            public void prepare(final Collection<? extends Input> resources) {
                executionOrder.put("input prepare", resources);
            }
        }

        private final class InternalHandler implements ResourceHandler<Internal> {

            @Override
            public void validate(final Collection<? extends Internal> resources) {
                executionOrder.put("internal validate", resources);
            }

            @Override
            public void prepare(final Collection<? extends Internal> resources) {
                executionOrder.put("internal prepare", resources);
            }
        }

        private final class OutputHandler implements ResourceHandler<Output> {
            @Override
            public void validate(final Collection<? extends Output> resources) {
                executionOrder.put("output validate", resources);
            }

            @Override
            public void ensure(final Collection<? extends Output> creatableResources) {
                executionOrder.put("output ensure", creatableResources);
            }

            @Override
            public void prepare(final Collection<? extends Output> resources) {
                executionOrder.put("output prepare", resources);
            }
        }
    }

    public static final class Input implements ComponentInput, UnownedResource {

        private final URI id = URI.create("java9-2:test-resource-input");

        public Input() {}

        @Override
        public URI id() {
            return id;
        }
    }

    public static final class Internal implements ComponentInternal {

        private final URI id = URI.create("java9-2:test-resource-internal");

        public Internal() {}

        @Override
        public URI id() {
            return id;
        }
    }

    public static final class Output implements ComponentOutput, OwnedResource {

        private final URI id = URI.create("java9-2:test-resource-output");

        public Output() {}

        @Override
        public URI id() {
            return id;
        }
    }
}

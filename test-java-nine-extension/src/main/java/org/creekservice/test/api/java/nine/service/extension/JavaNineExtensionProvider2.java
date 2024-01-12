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
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentInput;
import org.creekservice.api.platform.metadata.ComponentInternal;
import org.creekservice.api.platform.metadata.ComponentOutput;
import org.creekservice.api.platform.metadata.OwnedResource;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
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
                .addResource(Input.class, ext.new InputHandler())
                .addResource(Internal.class, ext.new InternalHandler())
                .addResource(Output.class, ext.new OutputHandler())
                .addResource(Inner.class, ext.new InnerHandler());

        return ext;
    }

    public static final class Extension implements CreekExtension {

        private static final String NAME = "java9_2";

        private final Collection<? extends ComponentDescriptor> components;
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
            public void validate(final Collection<? extends Input> resourceGroup) {
                executionOrder.put("input validate", resourceGroup);
            }

            @Override
            public void prepare(final Collection<? extends Input> resources) {
                executionOrder.put("input prepare", resources);
            }
        }

        private final class InternalHandler implements ResourceHandler<Internal> {

            @Override
            public void validate(final Collection<? extends Internal> resourceGroup) {
                executionOrder.put("internal validate", resourceGroup);
            }

            @Override
            public void prepare(final Collection<? extends Internal> resources) {
                executionOrder.put("internal prepare", resources);
            }
        }

        private final class OutputHandler implements ResourceHandler<Output> {
            @Override
            public void validate(final Collection<? extends Output> resourceGroup) {
                executionOrder.put("output validate", resourceGroup);
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

        private final class InnerHandler implements ResourceHandler<Inner> {
            @Override
            public void validate(final Collection<? extends Inner> resourceGroup) {
                executionOrder.put("inner validate", resourceGroup);
            }

            @Override
            public void ensure(final Collection<? extends Inner> creatableResources) {
                executionOrder.put("inner ensure", creatableResources);
            }

            @Override
            public void prepare(final Collection<? extends Inner> resources) {
                executionOrder.put("inner prepare", resources);
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

        @Override
        public Stream<ResourceDescriptor> resources() {
            return Stream.of(new Inner("inner:shared"), new Inner("inner:input"));
        }
    }

    public static final class Internal implements ComponentInternal {

        private final URI id = URI.create("java9-2:test-resource-internal");

        public Internal() {}

        @Override
        public URI id() {
            return id;
        }

        @Override
        public Stream<ResourceDescriptor> resources() {
            // 'this' included to test circular references are ignored:
            return Stream.of(new Inner("inner:shared"), new Inner("inner:internal"), this);
        }
    }

    public static final class Output implements ComponentOutput, OwnedResource {

        private final URI id = URI.create("java9-2:test-resource-output");

        @Override
        public URI id() {
            return id;
        }

        @Override
        public Stream<ResourceDescriptor> resources() {
            return Stream.of(new Inner("inner:shared"), new Inner("inner:output"));
        }
    }

    public static final class Inner implements OwnedResource {

        private final URI id;

        public Inner(final String id) {
            this.id = URI.create(requireNonNull(id, "id"));
        }

        @Override
        public URI id() {
            return id;
        }
    }
}

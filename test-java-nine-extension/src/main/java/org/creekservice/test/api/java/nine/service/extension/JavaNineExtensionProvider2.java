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
import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentInternal;
import org.creekservice.api.platform.metadata.ComponentOutput;
import org.creekservice.api.platform.metadata.ResourceHandler;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;

public final class JavaNineExtensionProvider2
        implements CreekExtensionProvider<JavaNineExtensionProvider2.Extension> {

    public JavaNineExtensionProvider2() {}

    @Override
    public Extension initialize(final CreekService api) {
        api.components()
                .model()
                .addResource(Internal.class, new InternalHandler())
                .addResource(Output.class, new OutputHandler());

        return new Extension(api.components().descriptors().stream().collect(Collectors.toList()));
    }

    private static final class InternalHandler implements ResourceHandler<Internal> {
        @Override
        public void ensure(final Collection<? extends Internal> resources) {}
    }

    private static final class OutputHandler implements ResourceHandler<Output> {
        @Override
        public void ensure(final Collection<? extends Output> resources) {}
    }

    public static final class Extension implements CreekExtension {

        private static final String NAME = "java9_2";

        private final Collection<? extends ComponentDescriptor> components;

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
    }

    public static final class Internal implements ComponentInternal {

        private final URI id = URI.create("java9-2:test-resource");

        public Internal() {}

        @Override
        public URI id() {
            return id;
        }
    }

    public static final class Output implements ComponentOutput {

        private final URI id = URI.create("java9-2:test-resource");

        public Output() {}

        @Override
        public URI id() {
            return id;
        }
    }
}

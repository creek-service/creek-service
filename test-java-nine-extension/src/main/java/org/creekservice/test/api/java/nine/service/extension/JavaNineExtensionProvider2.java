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

package org.creekservice.test.api.java.nine.service.extension;

import static java.util.Objects.requireNonNull;

import org.creekservice.api.platform.metadata.ComponentInternal;
import org.creekservice.api.platform.metadata.ComponentOutput;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.api.service.extension.model.ResourceHandler;

public final class JavaNineExtensionProvider2 implements CreekExtensionProvider {

    @Override
    public Extension initialize(final CreekService creek) {
        creek.model()
                .addResource(Internal.class, new ResourceHandler<>() {})
                .addResource(Output.class, new ResourceHandler<>() {});

        return new Extension(creek.service());
    }

    public static final class Extension implements CreekExtension {

        private static final String NAME = "java9_2";

        private final ServiceDescriptor service;

        public Extension(final ServiceDescriptor service) {
            this.service = requireNonNull(service, "service");
        }

        @Override
        public String name() {
            return NAME;
        }

        public ServiceDescriptor serviceDescriptor() {
            return service;
        }
    }

    public static final class Internal implements ComponentInternal {}

    public static final class Output implements ComponentOutput {}
}

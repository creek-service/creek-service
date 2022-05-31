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

import java.util.Optional;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.CreekExtension;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class JavaNineExtension implements CreekExtension {

    public static final String NAME = "java9";

    private final ServiceDescriptor service;
    private final Optional<JavaNineExtensionOptions> options;

    public JavaNineExtension(
            final ServiceDescriptor service, final Optional<JavaNineExtensionOptions> options) {
        this.service = requireNonNull(service, "service");
        this.options = requireNonNull(options, "options");
    }

    @Override
    public String name() {
        return NAME;
    }

    public ServiceDescriptor serviceDescriptor() {
        return service;
    }

    public Optional<JavaNineExtensionOptions> options() {
        return options;
    }
}

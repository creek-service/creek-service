/*
 * Copyright 2021-2022 Creek Contributors (https://github.com/creek-service)
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

package org.creek.api.service;


import org.creek.api.platform.metadata.ComponentDescriptor;
import org.creek.api.platform.metadata.ServiceDescriptor;
import org.creek.api.service.extension.CreekExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Defines the entry point for initialising Creek and getting hold of a {@link CreekContext}.
 */
public final class CreekContexts {

    public enum AppType {
        /**
         * The context will be used by a micro-service.
         */
        SERVICE,
        /**
         * The context will be used by a tool.
         */
        TOOL, // TODO
        /**
         * The context will be used by test code.
         */
        TEST // TODO
    }

    private CreekContexts() {
    }

    /**
     * Create an initializer for the supplied {@code service}.
     *
     * @param service the service descriptor
     * @return the initializer.
     */
    public static Builder forService(final ServiceDescriptor service) {
        return new Builder(AppType.SERVICE).withComponents(service);
    }

    public static final class Builder {

        private final AppType appType;
        private final List<ComponentDescriptor> components = new ArrayList<>();
        private final List<CreekExtension> extensions = new ArrayList<>();

        Builder(final AppType appType) {
            this.appType = requireNonNull(appType, "appType");
        }

        Builder withComponents(final ComponentDescriptor... components) {
            this.components.addAll(Arrays.asList(components));
            return this;
        }

        public Builder withExtensions(final CreekExtension... extensions) {
            // Todo(ac): Fail if type already registered.
            this.extensions.addAll(Arrays.asList(extensions));
            return this;
        }

        public CreekContext build() {
            return null; // TODO(AC)
        }
    }
}

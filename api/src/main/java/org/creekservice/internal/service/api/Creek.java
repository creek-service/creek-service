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

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.api.service.extension.component.ComponentDescriptorCollection;
import org.creekservice.internal.service.api.component.ComponentDescriptors;
import org.creekservice.internal.service.api.component.model.ComponentModel;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Creek implements CreekService {

    private final Options options;
    private final Components components;

    public Creek(final Collection<? extends ComponentDescriptor> components) {
        this(components, new ComponentModel());
    }

    public Creek(
            final Collection<? extends ComponentDescriptor> components,
            final ComponentModel model) {
        this(new Options(), model, components);
    }

    @VisibleForTesting
    Creek(
            final Options options,
            final ComponentModel model,
            final Collection<? extends ComponentDescriptor> components) {
        this.options = requireNonNull(options, "options");
        this.components = new Components(model, components);
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public Options options() {
        return options;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public Components components() {
        return components;
    }

    public void initializing(final Optional<CreekExtensionProvider<?>> provider) {
        components.model.initializing(provider);
    }

    public static final class Components implements ComponentAccessor {

        private final ComponentModel model;
        private final Descriptors descriptors;

        private Components(
                final ComponentModel model,
                final Collection<? extends ComponentDescriptor> components) {
            this.model = requireNonNull(model, "model");
            this.descriptors = new Descriptors(requireNonNull(components, "components"));
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public ComponentModel model() {
            return model;
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public Descriptors descriptors() {
            return descriptors;
        }
    }

    public static final class Descriptors implements ComponentDescriptorAccessor {

        private final ComponentDescriptors<AggregateDescriptor> aggregates;
        private final ComponentDescriptors<ServiceDescriptor> services;

        private Descriptors(final Collection<? extends ComponentDescriptor> components) {
            this.aggregates =
                    new ComponentDescriptors<>(filter(components, AggregateDescriptor.class));
            this.services = new ComponentDescriptors<>(filter(components, ServiceDescriptor.class));
        }

        @Override
        public ComponentDescriptorCollection<AggregateDescriptor> aggregates() {
            return aggregates;
        }

        @Override
        public ComponentDescriptorCollection<ServiceDescriptor> services() {
            return services;
        }

        private static <T> List<T> filter(
                final Collection<? extends ComponentDescriptor> components, final Class<T> type) {
            return components.stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .collect(Collectors.toList());
        }
    }
}

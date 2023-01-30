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

package org.creekservice.internal.service.api;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.api.service.extension.component.ComponentDescriptorCollection;
import org.creekservice.internal.service.api.component.ComponentDescriptors;
import org.creekservice.internal.service.api.component.model.ComponentModel;
import org.creekservice.internal.service.api.extension.Extensions;
import org.creekservice.internal.service.api.options.Options;

/** Implementation of {@link CreekService} */
public final class Creek implements CreekService {

    private final Options options;
    private final Components components;
    private final Extensions extensions;

    /**
     * @param components all known component descriptors
     */
    public Creek(final Collection<? extends ComponentDescriptor> components) {
        this(components, new Options(), Extensions::new, ComponentModel::new);
    }

    @VisibleForTesting
    Creek(
            final Collection<? extends ComponentDescriptor> components,
            final Options options,
            final Function<Creek, Extensions> extensions,
            final Function<Extensions, ComponentModel> model) {
        this.options = requireNonNull(options, "options");
        this.extensions = requireNonNull(extensions, "extensions").apply(this);
        this.components = new Components(model.apply(this.extensions), components);
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

    @Override
    public Extensions extensions() {
        return extensions;
    }

    /** Implementation of {@link ComponentAccessor} */
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

    /** Implementation of {@link ComponentDescriptorAccessor} */
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

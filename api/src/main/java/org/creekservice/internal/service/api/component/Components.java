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

package org.creekservice.internal.service.api.component;


import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.component.ComponentDescriptorCollection;
import org.creekservice.api.service.extension.component.ComponentDescriptors;

public final class Components implements ComponentDescriptors {

    private final DescriptorCollection<AggregateDescriptor> aggregates;
    private final DescriptorCollection<ServiceDescriptor> services;

    public Components(final Collection<? extends ComponentDescriptor> components) {
        this.aggregates = new DescriptorCollection<>(filter(components, AggregateDescriptor.class));
        this.services = new DescriptorCollection<>(filter(components, ServiceDescriptor.class));
    }

    @Override
    public ComponentDescriptorCollection<AggregateDescriptor> aggregate() {
        return aggregates;
    }

    @Override
    public ComponentDescriptorCollection<ServiceDescriptor> service() {
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

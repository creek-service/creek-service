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

package org.creekservice.api.service.extension.component;


import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;

/** Descriptors of components. */
public interface ComponentDescriptors {

    /**
     * Aggregate component descriptors.
     *
     * <p>These are the aggregates discovered on the class or module path.
     *
     * @return a collection of aggregate definitions.
     */
    ComponentDescriptorCollection<AggregateDescriptor> aggregate();

    /**
     * Service component descriptors.
     *
     * <p>These are the services discovered on the class or module path.
     *
     * @return a collection of service definitions.
     */
    ComponentDescriptorCollection<ServiceDescriptor> service();

    /** @return stream of all component descriptors. */
    default Stream<ComponentDescriptor> stream() {
        return Stream.concat(aggregate().stream(), service().stream());
    }
}

/*
 * Copyright 2023-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.service.extension.component.model;

import java.util.Collection;
import org.creekservice.api.platform.metadata.ResourceDescriptor;

/**
 * A Callback that Creek extensions implement to handler the resource types they support.
 *
 * @param <T> the specific resource type the handler handles.
 */
public interface ResourceHandler<T extends ResourceDescriptor> {

    /**
     * Validate that all the supplied {@code resourceGroup} consistently represent the same
     * resource.
     *
     * <p>There is often multiple resource descriptors describing the same resource present in the
     * system. For example, both input and output descriptors for the same resource. Any
     * inconsistencies in the details of the resource between these descriptors can lead to bugs.
     *
     * @param resourceGroup the set of resources that all share the same {@link
     *     ResourceDescriptor#id()}.
     * @throws RuntimeException with the details of any inconsistencies.
     */
    void validate(Collection<? extends T> resourceGroup);

    /**
     * Ensure the supplied external {@code creatableResources} exist.
     *
     * <p>Instructs an extension to ensure the resources described by the supplied descriptor exist
     * and are initialized.
     *
     * <p>Implementations should consider outputting a warning or failing if the resource already
     * exists, but does not match the expected configuration.
     *
     * @param creatableResources the resource instances to ensure exists and are initialized.
     *     Resources passed will be {@link org.creekservice.api.platform.metadata.CreatableResource
     *     creatable}.
     */
    default void ensure(Collection<? extends T> creatableResources) {
        throw new UnsupportedOperationException("Not a handler of owned resources");
    }

    /**
     * Called to allow the extension to initialise any internal state for the supplied {@code
     * resources}.
     *
     * @param resources the resource descriptors to prepare for.
     */
    void prepare(Collection<? extends T> resources);
}

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

package org.creekservice.api.service.extension.component.model;

import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ResourceHandler;

/** A collection of extensions to the Creek Service component model: */
public interface ComponentModelCollection {

    /**
     * The model collection knows about the supplied extension {@code type}.
     *
     * <p>Either the exact {@code type} has been added, or one of its super types.
     *
     * @param type the type to look up.
     * @return {@code true} if the collection knows about the type, {@code false} otherwise.
     */
    boolean hasType(Class<? extends ResourceDescriptor> type);

    /**
     * Retrieve a handler for a specific resource type.
     *
     * <p>If an exact match is found, it is returned. Otherwise, all handlers are searched for
     * compatible handlers. The compatible set is reduced by removing any super types of other types
     * with in the compatible set. The method will throw if the reduced set contains anything other
     * than a single compatible handler.
     *
     * @param resourceType the type of the resource descriptor.
     * @param <T> the type of the resource descriptor.
     * @return the registered resource handler.
     * @throws RuntimeException if no resource handler is registered, or if multiple registered
     *     handlers exist.
     */
    <T extends ResourceDescriptor> ResourceHandler<T> resourceHandler(Class<T> resourceType);
}

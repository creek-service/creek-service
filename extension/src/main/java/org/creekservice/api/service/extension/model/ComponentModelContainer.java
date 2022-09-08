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

package org.creekservice.api.service.extension.model;


import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ResourceHandler;

/** A mutable container of extensions to the Creek Service component model. */
public interface ComponentModelContainer extends ComponentModelCollection {

    /**
     * Register a custom resource type with Creek.
     *
     * <p>This can be a specific type, or a common super-type shared by the resource extensions the
     * extension needs to handle.
     *
     * <p>Resources can only be registered during the call to {@link
     * org.creekservice.api.service.extension.CreekExtensionProvider#initialize(org.creekservice.api.service.extension.CreekService)}
     *
     * @param type the custom recourse type.
     * @return self, to allow for method chaining.
     */
    <T extends ResourceDescriptor> ComponentModelContainer addResource(
            Class<T> type, ResourceHandler<? super T> handler);
}

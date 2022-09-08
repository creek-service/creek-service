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
}

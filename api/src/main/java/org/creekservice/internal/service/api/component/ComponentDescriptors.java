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

package org.creekservice.internal.service.api.component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.service.extension.component.ComponentDescriptorCollection;

/** Implementation of {@link ComponentDescriptorCollection} */
public final class ComponentDescriptors<T extends ComponentDescriptor>
        implements ComponentDescriptorCollection<T> {

    private final List<T> components;

    /**
     * @param components all known component descriptors
     */
    public ComponentDescriptors(final Collection<? extends T> components) {
        this.components = List.copyOf(components);
    }

    @Override
    public Iterator<T> iterator() {
        return components.iterator();
    }
}

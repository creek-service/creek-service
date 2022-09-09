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
import java.util.stream.StreamSupport;
import org.creekservice.api.platform.metadata.ComponentDescriptor;

/** Collection of component descriptors */
public interface ComponentDescriptorCollection<T extends ComponentDescriptor> extends Iterable<T> {

    /** @return stream of the defs the collection contains. */
    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}

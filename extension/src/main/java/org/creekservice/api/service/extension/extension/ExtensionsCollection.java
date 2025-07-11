/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.service.extension.extension;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.creekservice.api.service.extension.CreekExtension;

/** Read only collection of extensions to Creek service. */
public interface ExtensionsCollection extends Iterable<CreekExtension> {

    /**
     * Get an extension by type.
     *
     * @param extensionType the type of the extension to get.
     * @param <T> the type of the extension to get.
     * @return the extension, if installed.
     * @throws RuntimeException if not installed.
     */
    <T extends CreekExtension> T get(Class<T> extensionType);

    /**
     * @return stream of the extensions the collection contains.
     */
    default Stream<CreekExtension> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}

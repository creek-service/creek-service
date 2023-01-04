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

package org.creekservice.api.service.extension.extension;

import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;

/** Mutable container of the Creek extensions a.k.a. plugins that have been initialised. */
public interface ExtensionContainer extends ExtensionsCollection {
    /**
     * Ensure an extension has been initialized.
     *
     * <p>Initializes the extension from the supplied {@code providerType} if it has not already
     * been initialized. Does nothing if the extension has already been applied.
     *
     * @param providerType the provider type.
     * @param <T> the extension type.
     * @return the initialized extension.
     */
    <T extends CreekExtension> T ensureExtension(
            Class<? extends CreekExtensionProvider<T>> providerType);
}

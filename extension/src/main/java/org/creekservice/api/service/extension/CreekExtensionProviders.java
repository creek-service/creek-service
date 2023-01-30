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

package org.creekservice.api.service.extension;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/** Util class for working with {@link CreekExtensionProvider}. */
public final class CreekExtensionProviders {

    private CreekExtensionProviders() {}

    /**
     * Instantiate any extensions available at runtime.
     *
     * @return all extension providers found on class and module path
     */
    public static List<CreekExtensionProvider<?>> load() {
        return ServiceLoader.load(CreekExtensionProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .map(p -> (CreekExtensionProvider<?>) p)
                .collect(Collectors.toUnmodifiableList());
    }
}

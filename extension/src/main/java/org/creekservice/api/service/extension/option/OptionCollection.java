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

package org.creekservice.api.service.extension.option;

import java.util.Optional;
import org.creekservice.api.service.extension.CreekExtensionOptions;

/** A collection of {@link CreekExtensionOptions options} */
public interface OptionCollection {

    /**
     * Retrieve an option by type.
     *
     * <p>The supplied {@code type} can also be a super type or a registered option.
     *
     * @param type the type of option to retrieve.
     * @param <T> the type of the option to retrieve.
     * @return the option, if present, otherwise {@code empty}.
     * @throws RuntimeException if it is ambiguous which option should be returned, i.e. if multiple
     *     as subtypes of {@code type}.
     */
    <T extends CreekExtensionOptions> Optional<T> get(Class<T> type);
}

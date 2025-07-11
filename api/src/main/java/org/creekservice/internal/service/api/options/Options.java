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

package org.creekservice.internal.service.api.options;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.option.OptionContainer;
import org.creekservice.internal.service.api.util.SubTypeAwareMap;

/** Implementation of {@link OptionContainer} */
public final class Options implements OptionContainer {

    private final long threadId;
    private final Set<Class<? extends CreekExtensionOptions>> unused = new HashSet<>();
    private final SubTypeAwareMap<CreekExtensionOptions, CreekExtensionOptions> options =
            new SubTypeAwareMap<>();

    /** Constructor */
    public Options() {
        this(Thread.currentThread().getId());
    }

    @VisibleForTesting
    Options(final long threadId) {
        this.threadId = threadId;
    }

    @Override
    public void add(final CreekExtensionOptions option) {
        throwIfNotOnCorrectThread();

        options.compute(
                option.getClass(),
                (k, existing) -> {
                    if (existing != null) {
                        throw new IllegalArgumentException(
                                "Option of supplied type is already registered. type: "
                                        + option.getClass().getName());
                    }
                    return option;
                });

        unused.add(option.getClass());
    }

    @Override
    public <T extends CreekExtensionOptions> Optional<T> get(final Class<T> type) {
        throwIfNotOnCorrectThread();

        try {
            final Optional<T> option = options.getOrSuper(type).map(type::cast);
            option.map(CreekExtensionOptions::getClass).ifPresent(unused::remove);
            return option;
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                    "Requested option type is ambiguous: " + type.getName(), e);
        }
    }

    /**
     * @return extension options supplied by the user, but not used by any extension, i.e.
     *     superfluous options.
     */
    public Set<CreekExtensionOptions> unused() {
        throwIfNotOnCorrectThread();
        return unused.stream().map(options::get).collect(Collectors.toUnmodifiableSet());
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }
}

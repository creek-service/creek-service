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

package org.creek.internal.service.context;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.creek.api.service.context.CreekContext;
import org.creek.api.service.extension.CreekExtension;
import org.creekservice.api.base.type.temporal.Clock;

final class Context implements CreekContext {

    private final Clock clock;
    private final List<CreekExtension> extensions;

    Context(final Clock clock, final Collection<CreekExtension> extensions) {
        this.clock = requireNonNull(clock, "clock");
        this.extensions = List.copyOf(requireNonNull(extensions, "extensions"));
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CreekExtension> T extension(final Class<T> extensionType) {
        return (T)
                extensions.stream()
                        .filter(ext -> extensionType.isAssignableFrom(ext.getClass()))
                        .findFirst()
                        .orElseThrow(
                                () -> new UnknownExtensionException(extensionType, extensions));
    }

    private static class UnknownExtensionException extends IllegalArgumentException {
        UnknownExtensionException(
                final Class<?> extensionType, final Collection<CreekExtension> extensions) {
            super(
                    "No extension of requested type is registered: "
                            + extensionType
                            + ", installed_extensions: "
                            + extensions.stream()
                                    .map(CreekExtension::name)
                                    .collect(Collectors.toList()));
        }
    }
}

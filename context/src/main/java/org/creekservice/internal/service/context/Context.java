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

package org.creekservice.internal.service.context;

import static java.util.Objects.requireNonNull;

import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.service.context.CreekContext;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.internal.service.api.extension.Extensions;

final class Context implements CreekContext {

    private final Clock clock;
    private final Extensions extensions;

    Context(final Clock clock, final Extensions extensions) {
        this.clock = requireNonNull(clock, "clock");
        this.extensions = requireNonNull(extensions, "extensions");
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public <T extends CreekExtension> T extension(final Class<T> extensionType) {
        return extensions.get(extensionType);
    }

    @Override
    public void close() {
        extensions.close();
    }
}

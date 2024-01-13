/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.test.api.java.nine.service.extension;

import java.net.URI;
import org.creekservice.api.platform.metadata.ComponentInput;
import org.creekservice.api.platform.metadata.OwnedResource;

public final class JavaNineExtensionInput implements ComponentInput, OwnedResource {

    private final URI id = URI.create("java9:test-resource");

    public JavaNineExtensionInput() {}

    @Override
    public URI id() {
        return id;
    }
}

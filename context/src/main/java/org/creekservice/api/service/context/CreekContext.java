/*
 * Copyright 2021-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.service.context;

import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.service.extension.CreekExtension;

/** One-stop shop for access to Creek functionality. */
public interface CreekContext extends AutoCloseable {

    /**
     * Get the clock that services should use to determine the time.
     *
     * <p>Future working covered by <a
     * href="https://github.com/creek-service/creek-system-test/issues/158">System test #158</a>
     * will see the system tests support temporal sensitive functionality.
     *
     * @return the clock that services should use to determine the time. *
     */
    Clock clock();

    /**
     * Get access to one of the installed Creek extensions.
     *
     * @param extensionType the type of the extension.
     * @param <T> the type of the extension.
     * @return the extension, if installed
     * @throws IllegalArgumentException if not installed.
     */
    <T extends CreekExtension> T extension(Class<T> extensionType);

    /** Close all resources held by Creek. */
    void close();
}

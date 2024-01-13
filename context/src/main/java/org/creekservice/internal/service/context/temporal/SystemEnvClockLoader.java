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

package org.creekservice.internal.service.context.temporal;

import java.util.function.Supplier;
import org.creekservice.api.base.type.config.SystemEnv;
import org.creekservice.api.base.type.temporal.Clock;

/** Loads the impl of the clock to load from an environment variable. */
public final class SystemEnvClockLoader {

    /** Environment variable name to set to configure the {@link Clock} implementation to use. */
    public static final String ENV_VAR_NAME = "CREEK_CLOCK";

    /**
     * Load the clock impl from the environment variable, or use {@code defaultClock} if not set.
     *
     * @param defaultClock the default to use if the variable is not set
     * @return the clock
     */
    public Clock load(final Supplier<Clock> defaultClock) {
        return SystemEnv.readInstance(ENV_VAR_NAME, defaultClock);
    }
}

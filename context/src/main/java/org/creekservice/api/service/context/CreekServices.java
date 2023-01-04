/*
 * Copyright 2021-2022 Creek Contributors (https://github.com/creek-service)
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

import java.util.List;
import org.creekservice.api.base.type.temporal.Clock;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.CreekExtensionProviders;
import org.creekservice.internal.service.api.Creek;
import org.creekservice.internal.service.context.ContextBuilder;

/** Defines the entry point for initialising Creek and getting hold of a {@link CreekContext}. */
public final class CreekServices {

    private CreekServices() {}

    /**
     * Create a context for the supplied {@code service} without customizations.
     *
     * @param service the service descriptor
     * @return the context.
     */
    public static CreekContext context(final ServiceDescriptor service) {
        return builder(service).build();
    }

    /**
     * Create an context builder for the supplied {@code service}.
     *
     * @param service the service descriptor
     * @return the context builder.
     */
    public static Builder builder(final ServiceDescriptor service) {
        return new ContextBuilder(
                service, new Creek(List.of(service)), CreekExtensionProviders.load());
    }

    /** Builder of {@link CreekContext} */
    public interface Builder {

        /**
         * Set an explicit clock impl.
         *
         * <p>Note: the clock impl actually used can still be overridden at runtime via an
         * environment variable. See {@link
         * org.creekservice.internal.service.context.temporal.SystemEnvClockLoader}.
         *
         * @param clock the clock impl to use.
         * @return self.
         */
        Builder with(Clock clock);

        /**
         * Set extension options.
         *
         * @param options the options to set.
         * @return self.
         */
        Builder with(CreekExtensionOptions options);

        /**
         * @return the context the service should use.
         */
        CreekContext build();
    }
}

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

package org.creekservice.api.service.extension;


import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.model.ComponentModelContainer;
import org.creekservice.api.service.extension.option.OptionCollection;

/** The entry point to the API Creek exposes to service extensions. */
public interface CreekService {

    /**
     * The user supplied options used to configure extensions.
     *
     * <p>Extensions can add listeners for option types they are interested in.
     *
     * @return user options.
     */
    OptionCollection options();

    /**
     * The model used to define aggregate and service descriptors.
     *
     * <p>Extensions can extend this model.
     *
     * @return the model.
     */
    ComponentModelContainer model();

    /** @return the descriptor of the service being initialized. */
    ServiceDescriptor service();
}

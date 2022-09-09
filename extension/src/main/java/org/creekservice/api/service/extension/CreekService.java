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


import org.creekservice.api.service.extension.component.ComponentDescriptors;
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

    /**
     * The descriptors of components.
     *
     * <p>If running within the context of a microservice, this will return a single {@link
     * org.creekservice.api.platform.metadata.ServiceDescriptor}.
     *
     * <p>If running within the context of the system tests, this will contain all the components
     * the system test is aware of.
     *
     * @return component descriptor collection.
     */
    ComponentDescriptors components();
}

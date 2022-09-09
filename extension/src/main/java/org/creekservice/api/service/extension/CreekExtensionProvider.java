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

package org.creekservice.api.service.extension;


import java.util.Collection;
import org.creekservice.api.platform.metadata.ComponentDescriptor;

/**
 * Provider of an extension to Creek.
 *
 * <p>Creek will look for extension providers using {@link java.util.ServiceLoader} to load
 * instances of this type from the class & module paths. Therefore, to be loaded by Creek the
 * extension must:
 *
 * <ul>
 *   <li>be listed in the {@code module-info.java} file as a {@code provider} of {@link
 *       CreekExtensionProvider}, if using JPMS, or
 *   <li>have a suitable entry in the {@code META-INFO.services} directory, or
 *   <li>both of the above
 * </ul>
 */
public interface CreekExtensionProvider {

    /**
     * Called to allow the instance to initialise.
     *
     * <p>The instance can register custom resource types, add listeners, access user provided
     * options, and perform any initialisation needed for the returned extension.
     *
     * <p>The {@code components} parameter will contain a single {@link
     * org.creekservice.api.platform.metadata.ServiceDescriptor} if the extension is being
     * initialised as part of a normal microservice starting up, or multiple components if being
     * initialised as part of the system tests.
     *
     * @param api the API Creek exposes to extensions.
     * @param components the component or components to initialize the extension for.
     * @return the initialized extension.
     */
    CreekExtension initialize(
            CreekService api, Collection<? extends ComponentDescriptor> components);
}

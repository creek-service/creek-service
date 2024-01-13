/*
 * Copyright 2021-2024 Creek Contributors (https://github.com/creek-service)
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

/**
 * Provider of an extension to Creek.
 *
 * <p>Creek will look for extension providers using {@link java.util.ServiceLoader} to load
 * instances of this type from the class and module paths. Therefore, to be loaded by Creek the
 * extension must:
 *
 * <ul>
 *   <li>be listed in the {@code module-info.java} file as a {@code provider} of {@link
 *       CreekExtensionProvider}, if using JPMS, or
 *   <li>have a suitable entry in the {@code META-INFO.services} directory, or
 *   <li>both of the above
 * </ul>
 */
public interface CreekExtensionProvider<T extends CreekExtension> {

    /**
     * Called to allow the instance to initialise.
     *
     * <p>Extensions can register custom resource types, add listeners, access user provided
     * options, and perform any initialisation needed for the returned extension.
     *
     * <p>Extensions should also validate the resources of all {@link CreekService#components()
     * components} that the extension supports, and ensure any internal state to handle those
     * resources is initialised.
     *
     * <p>Where multiple resource descriptors describe the same resource, i.e. their {@link
     * org.creekservice.api.platform.metadata.ResourceDescriptor#id() id} is the same, extensions
     * should validate that all descriptors agree on the details, and throw a suitable exception
     * describing any discrepancies.
     *
     * @param api the API Creek exposes to extensions.
     * @return the initialized extension.
     */
    T initialize(CreekService api);
}

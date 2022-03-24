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

package org.creek.api.service.extension;


import org.creek.api.platform.metadata.ComponentDescriptor;
import org.creek.api.platform.metadata.ResourceDescriptor;

/**
 * Builder of extension to Creek.
 *
 * <p>Creek will look for extensions using {@link java.util.ServiceLoader} to load instances of
 * {@link CreekExtensionBuilder} from the class & module paths. To be loaded by Creek the extension
 * must be registered in either the {@code module-info.java} file as a {@code provider} of {@link
 * CreekExtensionBuilder} and/or have a suitable entry in the {@code META-INFO.services} directory.
 */
public interface CreekExtensionBuilder {

    /** @return the extension name. */
    String name();

    /**
     * Called to determine if an extension supports a specific resource.
     *
     * <p>Called by Creek to ensure each resource is handled by at least one extension.
     *
     * @param resourceDef the resource descriptor
     * @return {@code true} if the extension handles this type of resource, {@code false} otherwise.
     */
    boolean handles(ResourceDescriptor resourceDef);

    /**
     * Registers custom options for an extension with the builder.
     *
     * <p>Creek will pass all options instances to all extension builders. Implementations can
     * ignore any or all options. Creek will throw if no registered extension handles user supplied
     * options.
     *
     * @param options the custom options.
     * @return {@code true} if the extension supports this option type, {@code false} otherwise.
     */
    default boolean with(CreekExtensionOptions options) {
        return false;
    }

    /**
     * Build an initialized creek extension for the supplied {@code component}.
     *
     * @param component the component the extension should be initialized to handle.
     * @return the initialized extension.
     */
    CreekExtension build(ComponentDescriptor component);
}

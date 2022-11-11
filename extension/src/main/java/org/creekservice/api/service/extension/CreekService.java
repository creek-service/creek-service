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


import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.service.extension.component.ComponentDescriptorCollection;
import org.creekservice.api.service.extension.component.model.ComponentModelContainer;
import org.creekservice.api.service.extension.extension.ExtensionContainer;
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
    ComponentAccessor components();

    /**
     * Operations and information about the installed extensions.
     *
     * @return container for Creek service extensions, a.k.a. plugins.
     */
    ExtensionContainer extensions();

    /** Provides access to component metadata. */
    interface ComponentAccessor {

        /**
         * The model used to define aggregate and service descriptors.
         *
         * <p>Extensions can extend this model.
         *
         * @return the model.
         */
        ComponentModelContainer model();

        /**
         * The definitions of the known components, i.e. services and aggregates.
         *
         * @return the accessor to the component definitions.
         */
        ComponentDescriptorAccessor descriptors();
    }

    /** Provides access to component descriptor metadata */
    interface ComponentDescriptorAccessor {

        /**
         * Aggregate component descriptors.
         *
         * <p>These are the aggregates discovered on the class or module path.
         *
         * @return a collection of aggregate definitions.
         */
        ComponentDescriptorCollection<AggregateDescriptor> aggregates();

        /**
         * Service component descriptors.
         *
         * <p>These are the services discovered on the class or module path.
         *
         * @return a collection of service definitions.
         */
        ComponentDescriptorCollection<ServiceDescriptor> services();

        /**
         * @return stream of all component descriptors.
         */
        default Stream<ComponentDescriptor> stream() {
            return Stream.concat(aggregates().stream(), services().stream());
        }
    }
}

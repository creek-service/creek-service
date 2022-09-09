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

package org.creekservice.internal.service.api;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.Optional;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.internal.service.api.component.Components;
import org.creekservice.internal.service.api.model.ComponentModel;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Creek implements CreekService {

    private final ComponentModel model;
    private final Options options;
    private final Components components;

    public Creek(final Collection<? extends ComponentDescriptor> components) {
        this(new Options(), new ComponentModel(), new Components(components));
    }

    @VisibleForTesting
    Creek(final Options options, final ComponentModel model, final Components components) {
        this.options = requireNonNull(options, "options");
        this.model = requireNonNull(model, "model");
        this.components = requireNonNull(components, "components");
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public Options options() {
        return options;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public ComponentModel model() {
        return model;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public Components components() {
        return components;
    }

    public void initializing(final Optional<CreekExtensionProvider> provider) {
        model.initializing(provider);
    }
}

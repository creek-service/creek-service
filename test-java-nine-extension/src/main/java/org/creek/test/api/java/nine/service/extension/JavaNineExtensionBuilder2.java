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

package org.creek.test.api.java.nine.service.extension;


import org.creek.api.service.extension.CreekExtension;
import org.creek.api.service.extension.CreekExtensionBuilder;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentInternal;
import org.creekservice.api.platform.metadata.ComponentOutput;
import org.creekservice.api.platform.metadata.ResourceDescriptor;

public final class JavaNineExtensionBuilder2 implements CreekExtensionBuilder {

    private static final String NAME = "java9_2";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean handles(final ResourceDescriptor resourceDef) {
        return resourceDef instanceof Internal || resourceDef instanceof Output;
    }

    @Override
    public CreekExtension build(final ComponentDescriptor component) {
        return new Extension();
    }

    public static final class Extension implements CreekExtension {
        @Override
        public String name() {
            return NAME;
        }
    }

    public interface Internal extends ComponentInternal {}

    public interface Output extends ComponentOutput {}
}

/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.service.extension.component.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import org.creekservice.api.platform.metadata.ResourceDescriptor;

/** A mutable container of extensions to the Creek Service component model. */
public interface ComponentModelContainer extends ComponentModelCollection {

    /**
     * Register a custom resource type with Creek.
     *
     * <p>This can be a specific type, or a common super-type shared by the resource extensions the
     * extension needs to handle.
     *
     * <p>Resources can only be registered during the call to {@link
     * org.creekservice.api.service.extension.CreekExtensionProvider#initialize}
     *
     * @param type the custom recourse type.
     * @param handler the handler on which Creek will call back to have an extension handle a
     *     resource
     * @param <T> the custom resource type.
     * @return self, to allow for method chaining.
     */
    <T extends ResourceDescriptor> ComponentModelContainer addResource(
            Class<T> type, ResourceHandler<? super T> handler);

    /**
     * Register a custom resource type with Creek.
     *
     * <p>This is a convenience method to avoid having to cast resource and handlers that use
     * generics to raw types. The underlying registration is still by the raw class. It is therefore
     * not possible to register handlers for type refs that share the same raw class.
     *
     * <p>It is recommended that all type parameters of the {@code type} parameter are wildcards.
     * For example:
     *
     * <pre>{@code
     * interface MyResource<T> {
     *     ...
     * }
     *
     * class MyResourceHandler implements ResourceHandler<MyResource<?>> {
     *     ...
     * }
     *
     * api.components()
     *     .model()
     *     .addResource(new HandlerTypeRef<MyResourceHandler<?>>() {}, new MyResourceHandler());
     * }</pre>
     *
     * @param type the custom recourse type, which can contain generics.
     * @param handler the handler on which Creek will call back to have an extension handle a
     *     resource
     * @param <T> the custom resource type.
     * @return self, to allow for method chaining.
     * @see ComponentModelContainer#addResource(Class, ResourceHandler)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default <T extends ResourceDescriptor> ComponentModelContainer addResource(
            HandlerTypeRef<T> type, ResourceHandler<? super T> handler) {
        return addResource((Class) type.type, (ResourceHandler) handler);
    }

    /**
     * A help type used by {@link #addResource(HandlerTypeRef, ResourceHandler)}.
     *
     * @param <T> the type of the {@link ResourceDescriptor}
     * @see ComponentModelContainer#addResource(HandlerTypeRef, ResourceHandler)
     */
    @SuppressWarnings("unused")
    abstract class HandlerTypeRef<T extends ResourceDescriptor> {

        private final Class<?> type;

        /** Protected constructor to both subclassing */
        protected HandlerTypeRef() {
            final Type superClass = this.getClass().getGenericSuperclass();
            if (!(superClass instanceof ParameterizedType)) {
                throw new IllegalArgumentException("HandlerTypeRef constructed as raw type");
            }
            Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getRawType();
            }
            this.type = (Class<?>) type;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !HandlerTypeRef.class.isAssignableFrom(o.getClass())) {
                return false;
            }
            final HandlerTypeRef<?> that = (HandlerTypeRef<?>) o;
            return Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }

        @Override
        public String toString() {
            return "HandlerTypeRef<" + type.getName() + ">(){}";
        }
    }
}

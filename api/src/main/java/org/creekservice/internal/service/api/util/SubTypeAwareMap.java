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

package org.creekservice.internal.service.api.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Specialised map impl that provides a {@link #getOrSub} and {@link #getOrSuper} method that
 * supports lookups by subtype.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class SubTypeAwareMap<K, V> extends AbstractMap<Class<? extends K>, V>
        implements Map<Class<? extends K>, V> {

    private final Map<Class<? extends K>, V> types = new HashMap<>();

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<Entry<Class<? extends K>, V>> entrySet() {
        return types.entrySet();
    }

    @Override
    public V put(final Class<? extends K> key, final V value) {
        return types.put(key, value);
    }

    /**
     * Get the value associated with the supplied {@code key}, or the closest subtype.
     *
     * <p>If the map does not contain an exact match, the map is scanned for any subtypes of {@code
     * key}. Any type in the result that has a super type in the result is removed. For example, if
     * when looking up {@code A}, the subtype scan found {@code B} and {@code C}, and {@code C} was
     * a subtype of {@code B}, then it would be removed from the list and the method would return
     * {@code B}.
     *
     * <p>Any call that results in multiple potential subtypes will result in an exception.
     *
     * @param key the type to look up
     * @return The value associated with the supplied {@code key}, is present, otherwise the closest
     *     subtype if present, otherwise {@link Optional#empty()}.
     */
    public Optional<V> getOrSub(final Class<? extends K> key) {
        return find(key, e -> e.getKey().isAssignableFrom(key));
    }

    /**
     * Get the value associated with the supplied {@code key}, or the closest super type.
     *
     * @param key the type to look up
     * @return The value associated with the supplied {@code key}, is present, otherwise the closest
     *     supertype if present, otherwise {@link Optional#empty()}.
     */
    public Optional<V> getOrSuper(final Class<? extends K> key) {
        return find(key, e -> key.isAssignableFrom(e.getKey()));
    }

    private Optional<V> find(
            final Class<? extends K> key, final Predicate<Entry<Class<? extends K>, V>> filter) {
        final V exact = types.get(key);
        if (exact != null) {
            return Optional.of(exact);
        }

        final Map<Class<? extends K>, V> found =
                types.entrySet().stream()
                        .filter(filter)
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        final Map<Class<? extends K>, V> reduced = removeSuperTypes(found);

        switch (reduced.size()) {
            case 1:
                return Optional.of(reduced.values().iterator().next());
            case 0:
                return Optional.empty();
            default:
                throw new IllegalArgumentException(
                        "Ambiguous entry. Multiple entries match supplied key: "
                                + key.getName()
                                + ". Could be any of "
                                + reduced.keySet().stream()
                                        .map(Class::getName)
                                        .sorted()
                                        .collect(Collectors.joining(", ", "[", "]")));
        }
    }

    private Map<Class<? extends K>, V> removeSuperTypes(final Map<Class<? extends K>, V> types) {
        return types.entrySet().stream()
                .filter(
                        e ->
                                types.keySet().stream()
                                        .filter(t -> !t.equals(e.getKey()))
                                        .noneMatch(t -> e.getKey().isAssignableFrom(t)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

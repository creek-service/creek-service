/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class SubTypeAwareMapTest {

    private final SubTypeAwareMap<KeyType, String> map = new SubTypeAwareMap<>();

    @Test
    void shouldReturnEntrySet() {
        // Given:
        map.put(KeyTypeA.class, "a");

        // Then:
        assertThat(map, hasEntry(KeyTypeA.class, "a"));
    }

    @Test
    void shouldReturnEmptyIfNotFound() {
        assertThat(map.getOrSub(KeyTypeA.class), is(Optional.empty()));
        assertThat(map.getOrSuper(KeyTypeA.class), is(Optional.empty()));
    }

    @Test
    void shouldFindExactSub() {
        // Given:
        map.put(KeyTypeA.class, "a");

        // Then:
        assertThat(map.getOrSub(KeyTypeA.class), is(Optional.of("a")));
    }

    @Test
    void shouldFindExactSuper() {
        // Given:
        map.put(KeyTypeA.class, "a");

        // Then:
        assertThat(map.getOrSuper(KeyTypeA.class), is(Optional.of("a")));
    }

    @Test
    void shouldFindBySubType() {
        // Given:
        map.put(KeyType.class, "base");

        // Then:
        assertThat(map.getOrSub(KeyTypeA.class), is(Optional.of("base")));
    }

    @Test
    void shouldFindBySuperType() {
        // Given:
        map.put(KeyTypeA.class, "a");

        // Then:
        assertThat(map.getOrSuper(KeyType.class), is(Optional.of("a")));
    }

    @Test
    void shouldFindMostSpecificSubType() {
        // Given:
        map.put(KeyType.class, "base");
        map.put(KeyTypeA.class, "a");

        // Then:
        assertThat(map.getOrSub(KeyTypeAA.class), is(Optional.of("a")));
    }

    @Test
    void shouldFindMostSpecificSuperType() {
        // Given:
        map.put(KeyTypeAA.class, "aa");
        map.put(KeyTypeA.class, "a");

        // Then:
        assertThat(map.getOrSuper(KeyType.class), is(Optional.of("aa")));
    }

    @Test
    void shouldThrowOnAmbiguousSubType() {
        // Given:
        map.put(KeyType.class, "base");
        map.put(KeyTypeA.class, "a");
        map.put(KeyTypeB.class, "b");

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> map.getOrSub(KeyTypeM.class));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Ambiguous entry. Multiple entries match supplied key:"
                            + " org.creekservice.internal.service.api.util.SubTypeAwareMapTest$KeyTypeM."
                            + " Could be any of"
                            + " [org.creekservice.internal.service.api.util.SubTypeAwareMapTest$KeyTypeA,"
                            + " org.creekservice.internal.service.api.util.SubTypeAwareMapTest$KeyTypeB"
                            + "]"));
    }

    @Test
    void shouldThrowOnAmbiguousSuperType() {
        // Given:
        map.put(KeyTypeA.class, "a");
        map.put(KeyTypeB.class, "b");

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> map.getOrSuper(KeyType.class));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Ambiguous entry. Multiple entries match supplied key:"
                            + " org.creekservice.internal.service.api.util.SubTypeAwareMapTest$KeyType."
                            + " Could be any of"
                            + " [org.creekservice.internal.service.api.util.SubTypeAwareMapTest$KeyTypeA,"
                            + " org.creekservice.internal.service.api.util.SubTypeAwareMapTest$KeyTypeB"
                            + "]"));
    }

    private interface KeyType {}

    private interface KeyTypeA extends KeyType {}

    private interface KeyTypeAA extends KeyTypeA {}

    private interface KeyTypeB extends KeyType {}

    private interface KeyTypeM extends KeyTypeAA, KeyTypeB {}
}

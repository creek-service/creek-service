/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.test.service.java.nine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekExtensionProviders;
import org.junit.jupiter.api.Test;

class CreekExtensionProvidersTest {

    @Test
    void shouldLoadJava8Extension() {
        // When:
        final CreekExtensionProvider<?> ext = extByType("JavaEightExtensionProvider");

        // Then:
        assertThat(ext, is(notNullValue()));
        assertThat(
                ext.getClass().getName(),
                is(
                        "org.creekservice.test.java.eight.service.extension.JavaEightExtensionProvider"));
        assertThat(
                ext.getClass().getModule().getName(),
                is("creek.service.test.java.eight.extension"));
    }

    @Test
    void shouldLoadJava9Extension() {
        // When:
        final CreekExtensionProvider<?> ext = extByType("JavaNineExtensionProvider");

        // Then:
        assertThat(ext, is(notNullValue()));
        assertThat(
                ext.getClass().getName(),
                is(
                        "org.creekservice.test.internal.java.nine.service.extension.JavaNineExtensionProvider"));
        assertThat(
                ext.getClass().getModule().getName(), is("creek.service.test.java.nine.extension"));
    }

    private CreekExtensionProvider<?> extByType(final String className) {
        return CreekExtensionProviders.load().stream()
                .filter(ext -> ext.getClass().getSimpleName().equals(className))
                .findFirst()
                .orElse(null);
    }
}

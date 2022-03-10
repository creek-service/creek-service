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

package org.creek.test.service.extension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.creek.api.service.extension.CreekExtensionBuilder;
import org.junit.jupiter.api.Test;

class CreekExtensionsTest {

    @Test
    void shouldLoadJava8Extension() {
        // When:
        final CreekExtensionBuilder ext = extByType("JavaEightExtensionBuilder");

        // Then:
        assertThat(ext, is(notNullValue()));
        assertThat(
                ext.getClass().getName(),
                is("org.creek.test.java.eight.service.extension.JavaEightExtensionBuilder"));
        assertThat(
                ext.getClass().getModule().getName(),
                is("creek.service.test.java.eight.extension"));
    }

    @Test
    void shouldLoadJava9Extension() {
        // When:
        final CreekExtensionBuilder ext = extByType("JavaNineExtensionBuilder");

        // Then:
        assertThat(ext, is(notNullValue()));
        assertThat(
                ext.getClass().getName(),
                is("org.creek.test.java.nine.service.extension.JavaNineExtensionBuilder"));
        assertThat(
                ext.getClass().getModule().getName(), is("creek.service.test.java.nine.extension"));
    }

    private CreekExtensionBuilder extByType(final String className) {
        return ExtensionsLoader.load().stream()
                .filter(ext -> ext.getClass().getSimpleName().equals(className))
                .findFirst()
                .orElse(null);
    }
}

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


import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.test.api.java.nine.service.extension.JavaNineExtensionProvider2;
import org.creekservice.test.internal.java.nine.service.extension.JavaNineExtensionProvider;

module creek.service.test.java.nine.extension {
    requires transitive creek.service.extension;

    exports org.creekservice.test.api.java.nine.service.extension;

    provides CreekExtensionProvider with
            JavaNineExtensionProvider,
            JavaNineExtensionProvider2;
}

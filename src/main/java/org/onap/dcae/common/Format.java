/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcae.common;

public enum Format {
    JSON, XML, NONE;

    public static Format fromString(String s) {
        if ("json".equalsIgnoreCase(s)) {
            return JSON;
        }
        if ("xml".equalsIgnoreCase(s)) {
            return XML;
        }
        if ("none".equalsIgnoreCase(s)) {
            return NONE;
        }
        throw new IllegalArgumentException("Invalid value for format: " + s);
    }
}

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

public enum AuthType {
    NONE, BASIC, DIGEST, OAUTH, Unspecified;

    public static AuthType fromString(String s) {
        if ("basic".equalsIgnoreCase(s)) {
            return BASIC;
        }
        if ("digest".equalsIgnoreCase(s)) {
            return DIGEST;
        }
        if ("oauth".equalsIgnoreCase(s)) {
            return OAUTH;
        }
        if ("none".equalsIgnoreCase(s)) {
            return NONE;
        }
        if ("unspecified".equalsIgnoreCase(s)) {
            return Unspecified;
        }
        throw new IllegalArgumentException("Invalid value for format: " + s);
    }
}

/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
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

package org.onap.dcae.collectors.restconf.common;

public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH;

    public static HttpMethod fromString(String s) {
        if (s == null) {
            return null;
        }
        if (s.equalsIgnoreCase("get")) {
            return GET;
        }
        if (s.equalsIgnoreCase("post")) {
            return POST;
        }
        if (s.equalsIgnoreCase("put")) {
            return PUT;
        }
        if (s.equalsIgnoreCase("delete")) {
            return DELETE;
        }
        if (s.equalsIgnoreCase("patch")) {
            return PATCH;
        }
        throw new IllegalArgumentException("Invalid value for HTTP Method: " + s);
    }
}

/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2019 Huawei. All rights reserved.
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

public enum EventConnectionState {

    INIT, SUBSCRIBED, UNSUBSCRIBED, Unspecified;

    public static EventConnectionState fromString(String s) {
        if ("init".equalsIgnoreCase(s)) {
            return INIT;
        }
        if ("subscribed".equalsIgnoreCase(s)) {
            return SUBSCRIBED;
        }
        if ("unsubscribed".equalsIgnoreCase(s)) {
            return UNSUBSCRIBED;
        }
        if ("unspecified".equalsIgnoreCase(s)) {
            return Unspecified;
        }
        throw new IllegalArgumentException("Invalid value for format: " + s);
    }

}

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

import java.util.HashMap;
import java.util.Set;

public class RestConfContext {
    private HashMap<String, String> attributes;

    public RestConfContext() {
        attributes = new HashMap<>();
    }

    public String getAttribute(String name) {
        return attributes.getOrDefault(name, null);
    }

    public void setAttribute(String name, String value) {
        if (value == null) {
            if (attributes.containsKey(name)) {
                attributes.remove(name);
            }
        } else {
            attributes.put(name, value);
        }
    }

    public Set<String> getAttributeKeySet() {
        return attributes.keySet();
    }

}

/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2019 IBM. All rights reserved.
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

import org.junit.Test;
import org.onap.dcae.collectors.restconf.common.HttpMethod;

import static org.junit.Assert.*;

public class HttpMethodTest {

    @Test
    public void testFromString() {
        assertEquals(HttpMethod.GET, HttpMethod.fromString("get"));
        assertEquals(HttpMethod.POST, HttpMethod.fromString("post"));
        assertEquals(HttpMethod.PUT, HttpMethod.fromString("put"));
        assertEquals(HttpMethod.DELETE, HttpMethod.fromString("delete"));
        assertEquals(HttpMethod.PATCH, HttpMethod.fromString("patch"));
        assertNull(HttpMethod.fromString(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringWithException() {
        HttpMethod.fromString("test");
    }
}
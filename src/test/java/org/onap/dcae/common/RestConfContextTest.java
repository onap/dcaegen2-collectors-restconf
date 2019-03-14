/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
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

import org.junit.Test;
import org.onap.dcae.common.RestConfContext;

import static org.junit.Assert.*;

public class RestConfContextTest {

    @Test
    public void setAttribute() {
        String key = "key";
        String value = "value";
        RestConfContext restConfContext = new RestConfContext();
        restConfContext.setAttribute(key, value);
        assertEquals(value, restConfContext.getAttribute(key));
        restConfContext.setAttribute(key, null);
        assertFalse(restConfContext.getAttributeKeySet().contains(key));
    }

    @Test
    public void getAttributeKeySet() {
        String key = "key";
        String value = "value";
        String key1 = "key1";
        String value1 = "value1";
        RestConfContext restConfContext = new RestConfContext();
        restConfContext.setAttribute(key,value);
        restConfContext.setAttribute(key1,value1);
        assertTrue(restConfContext.getAttributeKeySet().contains(key) && restConfContext.getAttributeKeySet().contains(key1));
    }
}
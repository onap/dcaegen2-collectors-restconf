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

import static org.junit.Assert.assertEquals;

public class EventConnectionStateTest {

    @Test
    public void fromString() {
        assertEquals(EventConnectionState.INIT, EventConnectionState.fromString("init"));
        assertEquals(EventConnectionState.SUBSCRIBED, EventConnectionState.fromString("subscribed"));
        assertEquals(EventConnectionState.UNSUBSCRIBED, EventConnectionState.fromString("unsubscribed"));
        assertEquals(EventConnectionState.Unspecified, EventConnectionState.fromString("unspecified"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromStringWithException() {
        EventConnectionState.fromString("test");
    }
}
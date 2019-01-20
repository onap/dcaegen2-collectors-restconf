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

import static org.junit.Assert.*;

public class AuthTypeTest {

    @Test
    public void fromString() {
        assertEquals(AuthType.BASIC, AuthType.fromString("basic"));
        assertEquals(AuthType.DIGEST, AuthType.fromString("digest"));
        assertEquals(AuthType.OAUTH, AuthType.fromString("oauth"));
        assertEquals(AuthType.NONE, AuthType.fromString("none"));
        assertEquals(AuthType.Unspecified, AuthType.fromString("unspecified"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromStringWithException() {
        AuthType.fromString("test");
    }
}
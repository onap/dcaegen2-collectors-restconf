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

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RetryPolicyTest {

    String[] hostnames;
    Integer maximumRetries;
    RetryPolicy retryPolicy;

    @BeforeClass
    public void setUp() throws Exception {
        hostnames = "test1.com,test2.com,test3.com,4.4.4.4".split(",");
        maximumRetries = 3;
        retryPolicy = new RetryPolicy(this.hostnames, this.maximumRetries);
    }

    @Test
    public void testGetMaximumRetries() {
        assertEquals(maximumRetries, retryPolicy.getMaximumRetries());
    }

    @Test
    public void testGetNextHostName() throws RetryException {
        assertEquals(hostnames[2], retryPolicy.getNextHostName("http://" + hostnames[1] + "/endpoint"));
        assertEquals(hostnames[0], retryPolicy.getNextHostName("http://" + hostnames[3] + "/endpoint"));
    }

    @Test(expected = RetryException.class)
    public void testGetNextHostNameWithException() throws RetryException {
        retryPolicy.getNextHostName("abc");
    }
}
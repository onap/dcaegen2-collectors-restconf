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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class RetryPolicyStoreTest {

    RetryPolicyStore retryPolicyStore;
    String proxyServers;

    @Before
    public void setUp() throws Exception {
        retryPolicyStore = new RetryPolicyStore();
        proxyServers = "3.3.3.3,4.4.4.4,5.5.5.5";
    }

    @Test
    public void getProxyServers() {
        retryPolicyStore.setProxyServers(proxyServers);
        assertThat(retryPolicyStore.getProxyServers(), is(proxyServers));
    }

    @Test
    public void setProxyServers() {
        retryPolicyStore.setProxyServers(proxyServers);
        RetryPolicy actual = retryPolicyStore.getRetryPolicy("dme2proxy");
        String[] adminServersArray = {"3.3.3.3","4.4.4.4","5.5.5.5"};
        RetryPolicy expected = new RetryPolicy(adminServersArray, adminServersArray.length);
        assertThat(actual, is(expected));
    }

    @Test
    public void getRetryPolicy() {
        String[] adminServersArray = {"3.3.3.3","4.4.4.4","5.5.5.5"};
        RetryPolicy retryPolicy = new RetryPolicy(adminServersArray, adminServersArray.length);
        retryPolicyStore.retryPolicies.put("dme2proxy", retryPolicy);
        assertThat(retryPolicyStore.getRetryPolicy("dme2proxy"), is(retryPolicy));
    }
}
/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2022 Huawei. All rights reserved.
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

package org.onap.dcae.configuration.cbs;

import org.junit.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;

import static org.junit.Assert.assertEquals;

public class CbsClientConfigurationProviderTest {

    @Test
    public void CbsClientConfigurationTest () throws Exception {
        CbsClientConfigurationProvider cbsClientConfigurationProvider = new CbsClientConfigurationProvider();
        CbsClientConfiguration cbsClientConfiguration = cbsClientConfigurationProvider.get();
        assertEquals("config-binding-service", cbsClientConfiguration.hostname());
    }
}

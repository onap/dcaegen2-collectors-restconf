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

package org.onap.dcae.configuration;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.onap.dcae.configuration.cbs.CbsClientConfigurationProvider;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;

import java.time.Duration;

import static org.mockito.Mockito.mock;

public class ConfigurationHandlerTest {
    @Test
    public void startListenTest () throws Exception {
        CbsClientConfigurationProvider cbsClientConfigurationProvider = mock(CbsClientConfigurationProvider.class);
        ConfigUpdater configUpdater = mock(ConfigUpdater.class);
        ConfigurationHandler configurationHandler = new ConfigurationHandler(cbsClientConfigurationProvider,
                configUpdater);
        configurationHandler.startListen(Duration.ofMinutes(4L));
    }

    @Test
    public void createCbsClientTest () throws Exception {
        CbsClientConfigurationProvider cbsClientConfigurationProvider = mock(CbsClientConfigurationProvider.class);
        ConfigUpdater configUpdater = mock(ConfigUpdater.class);
        CbsClientConfiguration cbsClientConfiguration = mock(CbsClientConfiguration.class);
        ConfigurationHandler configurationHandler = new ConfigurationHandler(cbsClientConfigurationProvider,
                configUpdater);
        configurationHandler.createCbsClient(cbsClientConfiguration);
    }

    @Test
    public void handleConfigurationTest () throws Exception {
        try {
            CbsClientConfigurationProvider cbsClientConfigurationProvider = mock(CbsClientConfigurationProvider.class);
            ConfigUpdater configUpdater = mock(ConfigUpdater.class);
            CbsClientConfiguration cbsClientConfiguration = mock(CbsClientConfiguration.class);
            ConfigurationHandler configurationHandler = new ConfigurationHandler(cbsClientConfigurationProvider,
                    configUpdater);
            JsonObject jo = new JsonObject();
            jo.addProperty("config", "test");
            configurationHandler.handleConfiguration(jo);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }


}

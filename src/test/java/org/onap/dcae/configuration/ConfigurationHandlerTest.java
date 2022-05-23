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
import io.vavr.collection.Map;
import io.vavr.control.Try;
import org.junit.Test;
import org.onap.dcae.configuration.cbs.CbsClientConfigurationProvider;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import reactor.core.Disposable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ConfigurationHandlerTest {

    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path DMAAP_FILE = Paths.get(RESOURCES.toString(), "testDmaapConfig_ip.json");
    protected static final Path CONFIG_FILE = Paths.get(RESOURCES.toString(), "testcollector.properties");

    @Test
    public void startListenTest () throws Exception {
        CbsClientConfigurationProvider cbsClientConfigurationProvider = mock(CbsClientConfigurationProvider.class);
        ConfigUpdater configUpdater = mock(ConfigUpdater.class);
        ConfigurationHandler configurationHandler = new ConfigurationHandler(cbsClientConfigurationProvider,
                configUpdater);
        Disposable disposable = configurationHandler.startListen(Duration.ofMinutes(4L));
        assertTrue(disposable.isDisposed());
    }

    @Test
    public void handleConfigurationTest () throws Exception {
        CbsClientConfigurationProvider cbsClientConfigurationProvider = mock(CbsClientConfigurationProvider.class);
        ConfigUpdater configUpdater = mock(ConfigUpdater.class);
        configUpdater.setPaths(CONFIG_FILE, DMAAP_FILE);
        CbsClientConfiguration cbsClientConfiguration = mock(CbsClientConfiguration.class);
        ConfigurationHandler configurationHandler = new ConfigurationHandler(cbsClientConfigurationProvider,
                configUpdater);
        JsonObject jo = new JsonObject();
        JsonObject jo1 = new JsonObject();
        jo1.addProperty("config", "test");
        jo.add("config", jo1);
        configurationHandler.handleConfiguration(jo);
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        Try<Map<String, String>> sMap  = configFilesFacade.readCollectorProperties();
        sMap.onSuccess(value -> assertEquals(value.get("config").get(), "test"));
    }


}

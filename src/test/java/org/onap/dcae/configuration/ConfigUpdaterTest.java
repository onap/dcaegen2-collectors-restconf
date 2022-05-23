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

import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.dcae.ApplicationSettings;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigUpdaterTest {

    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path DMAAP_FILE = Paths.get(RESOURCES.toString(), "testDmaapConfig_ip.json");
    protected static final Path CONFIG_FILE = Paths.get(RESOURCES.toString(), "testcollector.properties");

    @Test
    public void setPathTest() throws Exception {

        ConfigFilesFacade configFilesFacade = mock(ConfigFilesFacade.class);
        Runnable runnable = mock(Runnable.class);
        ApplicationSettings properties = mock(ApplicationSettings.class);
        when(properties.configurationFileLocation()).thenReturn(CONFIG_FILE);
        when(properties.dMaaPConfigurationFileLocation()).thenReturn(DMAAP_FILE.toString());
        ConfigUpdater configUpdater = new ConfigUpdater(configFilesFacade, runnable);
        configUpdater.setPaths(properties.configurationFileLocation() ,
                Paths.get(properties.dMaaPConfigurationFileLocation()));
        String sFileData = new String(Files.readAllBytes(CONFIG_FILE), StandardCharsets.UTF_8);
        assertTrue(sFileData.contains("collector.rcc.service.port=9999"));
    }

    @Test
    public void updateConfigTest() throws Exception {
        try {
            JSONObject jo = new JSONObject("{\"collector.dmaapfile\":\"test\"}");
            ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
            Runnable runnable = mock(Runnable.class);
            ConfigUpdater configUpdater = new ConfigUpdater(configFilesFacade, runnable);
            configUpdater.updateConfig(Option.of(jo));
            Try<Map<String, String>> sMap  = configFilesFacade.readCollectorProperties();
            sMap.onSuccess(value -> assertEquals(value.get("collector.rcc.service.secure.port").get(), "8687"));
        } catch (Exception ex) {

        }
    }




}

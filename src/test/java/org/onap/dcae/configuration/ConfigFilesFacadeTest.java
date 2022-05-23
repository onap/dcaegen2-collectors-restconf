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

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Try;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigFilesFacadeTest {

    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path DMAAP_FILE = Paths.get(RESOURCES.toString(), "testDmaapConfig_ip.json");
    protected static final Path CONFIG_FILE = Paths.get(RESOURCES.toString(), "testcollector.properties");


    @Test
    public void setPathsTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        configFilesFacade.setPaths(CONFIG_FILE, DMAAP_FILE);
        String sFileData = new String(Files.readAllBytes(CONFIG_FILE), StandardCharsets.UTF_8);
        assertTrue(sFileData.contains("config=test"));
    }

    @Test
    public void readCollectorPropertiesTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        Try<Map<String, String>> sMap  = configFilesFacade.readCollectorProperties();
        sMap.onSuccess(value -> assertEquals(value.get("collector.rcc.service.secure.port").get(), "8687"));
    }

    @Test
    public void readDMaaPConfigurationTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        Try<JSONObject> job  = configFilesFacade.readDMaaPConfiguration();
        job.onSuccess(value -> assertEquals("test1", value.get("config1")));
    }

    @Test
    public void writeDMaaPConfigurationTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        JSONObject jo = new JSONObject("{\"config1\":\"test1\"}");
        configFilesFacade.writeDMaaPConfiguration(jo);
        Try<JSONObject> job  = configFilesFacade.readDMaaPConfiguration();
        job.onSuccess(value -> assertEquals("test1", value.get("config1")));
    }

    @Test
    public void writePropertiesTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        Map<String, String> jo = HashMap.of("config2", "test2");
        configFilesFacade.writeProperties(jo);
        Try<Map<String, String>> sMap  = configFilesFacade.readCollectorProperties();
        sMap.onSuccess(value -> assertEquals("test2", value.get("config2").get()));
    }
}

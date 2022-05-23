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
import org.json.JSONObject;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigFilesFacadeTest {

    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path DMAAP_FILE = Paths.get(RESOURCES.toString(), "testDmaapConfig_ip.json");
    protected static final Path CONFIG_FILE = Paths.get(RESOURCES.toString(), "testcollector.properties");


    @Test
    public void setPathsTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        configFilesFacade.setPaths(CONFIG_FILE, DMAAP_FILE);
    }

    @Test
    public void readCollectorPropertiesTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        configFilesFacade.readCollectorProperties();
    }

    @Test
    public void readDMaaPConfigurationTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        configFilesFacade.readDMaaPConfiguration();
    }

    @Test
    public void writeDMaaPConfigurationTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        JSONObject jo = new JSONObject("{\"config\":\"test\"}");
        configFilesFacade.writeDMaaPConfiguration(jo);
    }

    @Test
    public void writePropertiesTest () throws Exception {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(CONFIG_FILE, DMAAP_FILE);
        Map<String, String> jo = HashMap.of("config", "test");
        configFilesFacade.writeProperties(jo);
    }
}

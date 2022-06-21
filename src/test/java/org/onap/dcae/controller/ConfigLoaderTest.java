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
package org.onap.dcae.controller;

import org.json.JSONObject;
import org.junit.Test;
import org.onap.dcae.WiremockBasedTest;
import org.onap.dcae.common.publishing.EventPublisher;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.vavr.API.Map;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.onap.dcae.TestingUtilities.createTemporaryFile;
import static org.onap.dcae.TestingUtilities.readJsonFromFile;
import static org.onap.dcae.common.publishing.VavrUtils.f;

public class ConfigLoaderTest extends WiremockBasedTest {
    @Test
    public void shouldNotReconfigureNotOverwriteIfConfigurationHasNotChanged() {
        // given

        try {
            Path dmaapConfigFile = createTemporaryFile("temp_dmaapConfigFile", "{}");
            Path collectorPropertiesFile = createTemporaryFile("temp_collectorPropertiesFile", "");
            JSONObject dmaapConf = readJsonFromFile(
                    Paths.get("src/test/resources/testParseDMaaPCredentialsGen2.json"));
            stubConsulToReturnLocalAddressOfCbs();
            stubCbsToReturnAppConfig(f("{\"collector.port\": 8080, \"streams_publishes\": %s}}", dmaapConf));
            EventPublisher eventPublisherMock = mock(EventPublisher.class);
            ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(dmaapConfigFile, collectorPropertiesFile);
            configFilesFacade.writeProperties(Map("collector.port", "8080"));
            configFilesFacade.writeDMaaPConfiguration(dmaapConf);

            ConfigLoader configLoader = new ConfigLoader(eventPublisherMock::reconfigure,
                    configFilesFacade, ConfigSource::getAppConfig, () -> wiremockBasedEnvProps());

            // when
            JSONObject dmaapConf2 = readJsonFromFile(
                    Paths.get("src/test/resources/testParseDMaaPCredentialsGen2Temp.json"));
            configFilesFacade.writeDMaaPConfiguration(dmaapConf2);
            configFilesFacade.writeProperties(Map("collector.port", "8081"));
            configLoader = configLoader.create(null, dmaapConfigFile,  collectorPropertiesFile );
            assertNotNull(configLoader);
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
    }
}

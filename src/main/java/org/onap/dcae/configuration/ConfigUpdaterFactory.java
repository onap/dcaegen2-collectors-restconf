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

import java.nio.file.Path;

import org.onap.dcae.RestConfCollector;

/**
 * ConfigUpdaterFactory is responsible for receiving configuration from config file or Consul (if config file doesn't exist).
 */
public class ConfigUpdaterFactory {

    private ConfigUpdaterFactory() {
    }

    /**
     * create configuration updater based on property file and dmaap configuration files
     * @param propertiesFile application property file
     * @param dmaapConfigFile dmaap configuration file
     */
    public static ConfigUpdater create(Path propertiesFile, Path dmaapConfigFile) {
        ConfigFilesFacade configFilesFacade = new ConfigFilesFacade(propertiesFile, dmaapConfigFile);
        return new ConfigUpdater(
            configFilesFacade,
                RestConfCollector::restartApplication);
    }
}

/*
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2018 Nokia. All rights reserved.
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

package org.onap.dcae;

import com.google.common.annotations.VisibleForTesting;
import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Abstraction over application configuration.
 * Its job is to provide easily discoverable (by method names lookup) and type safe access to configuration properties.
 */
public class ApplicationSettings {

    private static final Logger log = LoggerFactory.getLogger(ApplicationSettings.class);
    public static String responseCompatibility;
    private final String appInvocationDir;
    private final String configurationFileLocation;
    private final PropertiesConfiguration properties = new PropertiesConfiguration();


    public ApplicationSettings(String[] args, Function1<String[], Map<String, String>> argsParser) {
        this(args, argsParser, System.getProperty("user.dir"));
    }

    public ApplicationSettings(String[] args, Function1<String[], Map<String, String>> argsParser, String appInvocationDir) {
        log.info("New ApplicationSettings........");
        this.appInvocationDir = appInvocationDir;
        properties.setDelimiterParsingDisabled(true);
        Map<String, String> parsedArgs = argsParser.apply(args);
        configurationFileLocation = findOutConfigurationFileLocation(parsedArgs);
        loadPropertiesFromFile();
        parsedArgs.filterKeys(k -> !"c".equals(k)).forEach(this::addOrUpdate);
    }


    public void reloadProperties() {
        try {
            log.info("Reloading Properties ....");
            properties.load(configurationFileLocation);
            properties.refresh();
        } catch (ConfigurationException ex) {
            log.error("Cannot load properties cause:", ex);
            throw new ApplicationException(ex);
        }
    }

    public void loadPropertiesFromFile() {
        try {
            properties.load(configurationFileLocation);
            Iterator<String> itr = properties.getKeys();
            while (itr.hasNext()) {
                String key = itr.next();
                log.info(" Key " + key + " value" + properties.getString(key));
            }
        } catch (ConfigurationException ex) {
            log.error("Cannot load properties cause:", ex);
            throw new ApplicationException(ex);
        }
    }


    public boolean authorizationEnabled() {
        return properties.getInt("collector.header.authflag", 0) > 0;
    }

    private String findOutConfigurationFileLocation(Map<String, String> parsedArgs) {
        return prependWithUserDirOnRelative(parsedArgs.get("c").getOrElse("etc/collector.properties"));
    }

    public Path configurationFileLocation() {
        return Paths.get(configurationFileLocation);
    }

    public int maximumAllowedQueuedEvents() {
        return properties.getInt("collector.rcc.inputQueue.maxPending", 1024 * 4);
    }

    public int httpPort() {
        return properties.getInt("collector.rcc.service.port", 8080);
    }

    public int httpsPort() {
        return properties.getInt("collector.rcc.service.secure.port", 8687);
    }


    public boolean httpsEnabled() {
        return httpsPort() > 0;
    }

    public String rccKeystorePasswordFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.keystore.passwordfile", "etc/rcc_passwordfile"));
    }

    public String rccKeystoreFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.keystore.file.location", "etc/keystore"));
    }

    public String keystorePasswordFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.rcc.keystore.passwordfile", "etc/passwordfile"));
    }

    public String keystoreFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.rcc.keystore.file.location", "etc/sdnc.p12"));
    }

    public boolean clientTlsAuthenticationEnabled() {
        return httpsEnabled() && properties.getInt("collector.rcc.service.secure.clientauth", 0) > 0;
    }

    public Map<String, String> validAuthorizationCredentials() {
        return prepareUsersMap(properties.getString("collector.header.authlist", null));
    }

    private Map<String, String> prepareUsersMap(@Nullable String allowedUsers) {
        return allowedUsers == null ? HashMap.empty()
                : List.of(allowedUsers.split("\\|"))
                .map(t -> t.split(","))
                .toMap(t -> t[0].trim(), t -> t[1].trim());
    }

    public String keystoreAlias() {
        return properties.getString("collector.rcc.keystore.alias", "tomcat");
    }

    public String truststorePasswordFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.rcc.truststore.passwordfile", "etc/trustpasswordfile"));
    }

    public String truststoreFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.rcc.truststore.file.location", "etc/truststore.onap.client.jks"));
    }

    public String rccPolicy() {
        return properties.getString("rcc_policy", "");
    }

    public String dMaaPConfigurationFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.dmaapfile", "etc/DmaapConfig.json"));
    }

    public String controllerConfigFileLocation() {
        return prependWithUserDirOnRelative(properties.getString("collector.eventinfo", "etc/ont_config.json"));
    }

    public int configurationUpdateFrequency() {
        return properties.getInt("collector.dynamic.config.update.frequency", 5);
    }

    public String dMaaPStreamsMapping() {
        return properties.getString("collector.rcc.dmaap.streamid", null);
    }

    public void addOrUpdate(String key, String value) {
        if (properties.containsKey(key)) {
            properties.setProperty(key, value);
        } else {
            properties.addProperty(key, value);
        }
    }


    private String prependWithUserDirOnRelative(String filePath) {
        if (!Paths.get(filePath).isAbsolute()) {
            filePath = Paths.get(appInvocationDir, filePath).toString();
        }
        return filePath;
    }

    @VisibleForTesting
    String getStringDirectly(String key) {
        return properties.getString(key);
    }
}


/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;
import static org.onap.dcae.CLIUtils.processCmdLine;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import org.junit.Test;

public class ApplicationSettingsTest {

    @Test
    public void shouldMakeApplicationSettingsOutOfCliArguments() {
        // given
        String[] cliArguments = {"-param1", "param1value", "-param2", "param2value"};

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        String param1value = configurationAccessor.getStringDirectly("param1");
        String param2value = configurationAccessor.getStringDirectly("param2");

        // then
        assertEquals("param1value", param1value);
        assertEquals("param2value", param2value);
    }

    @Test
    public void shouldMakeApplicationSettingsOutOfCliArgumentsAndAConfigurationFile()
        throws IOException {
        // given
        File tempConfFile = File.createTempFile("doesNotMatter", "doesNotMatter");
        Files.write(tempConfFile.toPath(), Arrays.asList("section.subSection1=abc", "section.subSection2=zxc"));
        tempConfFile.deleteOnExit();
        String[] cliArguments = {"-param1", "param1value", "-param2", "param2value", "-c", tempConfFile.toString()};

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        String param1value = configurationAccessor.getStringDirectly("param1");
        String param2value = configurationAccessor.getStringDirectly("param2");

        // then
        assertEquals("param1value", param1value);
        assertEquals("param2value", param2value);

        String fromFileParam1Value = configurationAccessor.getStringDirectly("section.subSection1");
        String fromFileParam2Value = configurationAccessor.getStringDirectly("section.subSection2");

        // then
        assertEquals("abc", fromFileParam1Value);
        assertEquals("zxc", fromFileParam2Value);
    }

    @Test
    public void shouldCliArgumentsOverrideConfigFileParameters() throws IOException {
        // given
        String[] cliArguments = {"-section.subSection1", "abc"};
        File tempConfFile = File.createTempFile("doesNotMatter", "doesNotMatter");
        Files.write(tempConfFile.toPath(), singletonList("section.subSection1=zxc"));
        tempConfFile.deleteOnExit();

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        String actuallyOverridenByCliParam = configurationAccessor.getStringDirectly("section.subSection1");

        // then
        assertEquals("abc", actuallyOverridenByCliParam);

        configurationAccessor.reloadProperties();

        boolean auth = configurationAccessor.clientTlsAuthenticationEnabled();
        assertEquals(auth, false);
    }

    @Test
    public void shouldReturnHttpPort() throws IOException {
        // when
        int applicationPort = fromTemporaryConfiguration("collector.rcc.service.port=8090")
            .httpPort();

        // then
        assertEquals(8090, applicationPort);
    }

    @Test
    public void shouldLoadPropertiesFromFile() throws IOException {
        // given
        String[] cliArguments = {"-section.subSection1", "abc"};
        File tempConfFile = File.createTempFile("doesNotMatter", "doesNotMatter");
        Files.write(tempConfFile.toPath(), singletonList("section.subSection1=zxc"));
        tempConfFile.deleteOnExit();

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        String actuallyOverridenByCliParam = configurationAccessor.getStringDirectly("section.subSection1");

        // then
        assertEquals("abc", actuallyOverridenByCliParam);

        configurationAccessor.loadPropertiesFromFile();

        boolean auth = configurationAccessor.clientTlsAuthenticationEnabled();
        assertEquals(auth, false);
    }

    @Test
    public void shouldReturnDefaultHttpPort() throws IOException {
        // when
        int applicationPort = fromTemporaryConfiguration().httpPort();

        // then
        assertEquals(8080, applicationPort);
    }

    @Test
    public void shouldReturnIfHttpSIsEnabled() throws IOException {
        // when
        boolean httpsEnabled = fromTemporaryConfiguration("collector.rcc.service.secure.port=8687")
            .httpsEnabled();

        // then
        assertTrue(httpsEnabled);
    }

    @Test
    public void shouldReturnIfHttpIsEnabled() throws IOException {
        // when
        boolean httpsEnabled = fromTemporaryConfiguration("collector.service.port=8080").httpsEnabled();
        // then
        assertTrue(httpsEnabled);
    }

    @Test
    public void shouldByDefaultHttpSBeDisabled() throws IOException {
        // when
        boolean httpsEnabled = fromTemporaryConfiguration().httpsEnabled();

        // then
        assertTrue(httpsEnabled);
    }

    @Test
    public void shouldReturnHttpSPort() throws IOException {
        // when
        int httpsPort = fromTemporaryConfiguration("collector.service.secure.port=8687")
            .httpsPort();

        // then
        assertEquals(8687, httpsPort);
    }


    @Test
    public void shouldReturnLocationOfThePasswordFile() throws IOException {
        // when
        String passwordFileLocation = fromTemporaryConfiguration(
                "collector.rcc.keystore.passwordfile=/somewhere/password")
            .keystorePasswordFileLocation();

        // then
        assertEquals(sanitizePath("/somewhere/password"), passwordFileLocation);
    }

    @Test
    public void shouldReturnDefaultLocationOfThePasswordFile() throws IOException {
        // when
        String passwordFileLocation = fromTemporaryConfiguration().keystorePasswordFileLocation();

        // then
        assertEquals(sanitizePath("etc/passwordfile"), passwordFileLocation);
    }

    @Test
    public void shouldReturnLocationOfTheKeystoreFile() throws IOException {
        // when
        String keystoreFileLocation = fromTemporaryConfiguration(
                "collector.rcc.keystore.file.location=/somewhere/keystore")
            .keystoreFileLocation();

        // then
        assertEquals(sanitizePath("/somewhere/keystore"), keystoreFileLocation);
    }

    @Test
    public void shouldReturnLocationOfTheDefaultKeystoreFile() throws IOException {
        // when
        String keystoreFileLocation = fromTemporaryConfiguration().keystoreFileLocation();

        // then
        assertEquals(sanitizePath("etc/sdnc.p12"), keystoreFileLocation);
    }

    @Test
    public void shouldReturnDmaapConfigFileLocation() throws IOException {
        // when
        String dmaapConfigFileLocation = fromTemporaryConfiguration(
                "collector.dmaapfile=/somewhere/dmaapFile")
            .dMaaPConfigurationFileLocation();

        // then
        assertEquals(sanitizePath("/somewhere/dmaapFile"), dmaapConfigFileLocation);
    }

    @Test
    public void shouldReturnDefaultDmaapConfigFileLocation() throws IOException {
        // when
        String dmaapConfigFileLocation = fromTemporaryConfiguration().dMaaPConfigurationFileLocation();

        // then
        assertEquals(sanitizePath("etc/DmaapConfig.json"), dmaapConfigFileLocation);
    }

    @Test
    public void shouldReturnMaximumAllowedQueuedEvents() throws IOException {
        // when
        int maximumAllowedQueuedEvents = fromTemporaryConfiguration(
                "collector.rcc.inputQueue.maxPending=10000")
            .maximumAllowedQueuedEvents();

        // then
        assertEquals(10000, maximumAllowedQueuedEvents);
    }

    @Test
    public void shouldReturnDefaultMaximumAllowedQueuedEvents() throws IOException {
        // when
        int maximumAllowedQueuedEvents = fromTemporaryConfiguration().maximumAllowedQueuedEvents();

        // then
        assertEquals(1024 * 4, maximumAllowedQueuedEvents);
    }

    @Test
    public void shouldReturnIfAuthorizationIsEnabled() throws IOException {
        // when
        boolean authorizationEnabled = fromTemporaryConfiguration("collector.header.authflag=1")
            .authorizationEnabled();

        // then
        assertTrue(authorizationEnabled);
    }

    @Test
    public void shouldAuthorizationBeDisabledByDefault() throws IOException {
        // when
        boolean authorizationEnabled = fromTemporaryConfiguration().authorizationEnabled();

        // then
        assertFalse(authorizationEnabled);
    }

    @Test
    public void shouldReturnValidCredentials() throws IOException {
        // when
        Map<String, String> allowedUsers = fromTemporaryConfiguration(
            "collector.header.authlist=pasza,c2ltcGxlcGFzc3dvcmQNCg==|someoneelse,c2ltcGxlcGFzc3dvcmQNCg=="
        ).validAuthorizationCredentials();

        // then
        assertEquals(allowedUsers.get("pasza").get(), "c2ltcGxlcGFzc3dvcmQNCg==");
        assertEquals(allowedUsers.get("someoneelse").get(), "c2ltcGxlcGFzc3dvcmQNCg==");
    }

    @Test
    public void shouldbyDefaultThereShouldBeNoValidCredentials() throws IOException {
        // when
        Map<String, String> userToBase64PasswordDelimitedByCommaSeparatedByPipes = fromTemporaryConfiguration()
                .validAuthorizationCredentials();

        // then
        assertTrue(userToBase64PasswordDelimitedByCommaSeparatedByPipes.isEmpty());
    }


    @Test
    public void shouldReturnCambriaConfigurationFileLocation() throws IOException {
        // when
        String cambriaConfigurationFileLocation = fromTemporaryConfiguration(
            "collector.dmaapfile=/somewhere/dmaapConfig")
            .dMaaPConfigurationFileLocation();

        // then
        assertEquals(sanitizePath("/somewhere/dmaapConfig"), cambriaConfigurationFileLocation);
    }

    @Test
    public void shouldReturnDefaultCambriaConfigurationFileLocation() throws IOException {
        // when
        String cambriaConfigurationFileLocation = fromTemporaryConfiguration()
            .dMaaPConfigurationFileLocation();

        // then
        assertEquals(sanitizePath("etc/DmaapConfig.json"), cambriaConfigurationFileLocation);
    }

    @Test
    public void shouldConfigurationFileLocation() throws IOException {

        try {
            String[] cliArguments = {"-param1", "param1value", "-param2", "param2value"};
            // when
            ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
            Path path = configurationAccessor.configurationFileLocation();
            assertTrue(path.endsWith("collector.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldrccKeystorePathExistDefault() throws IOException {
        // when
        String path = fromTemporaryConfiguration().rccKeystoreFileLocation();

        // then
        assertEquals(sanitizePath("etc/keystore"), path);
    }

    @Test
    public void shouldrccKeystorePasswordExistDefault() throws IOException {
        // when
        String path = fromTemporaryConfiguration().rccKeystorePasswordFileLocation();

        // then
        assertEquals(sanitizePath("etc/rcc_passwordfile"), path);
    }

    @Test
    public void shouldTruststorePathExistDefault() throws IOException {
        // when
        String path = fromTemporaryConfiguration().truststorePasswordFileLocation();

        // then
        assertEquals(sanitizePath("etc/trustpasswordfile"), path);
    }

    @Test
    public void shouldTruststorePasswordExistDefault() throws IOException {
        // when
        String path = fromTemporaryConfiguration().truststoreFileLocation();

        // then
        assertEquals(sanitizePath("etc/truststore.onap.client.jks"), path);
    }

    @Test
    public void shouldHaveKeyStoreAlias() throws IOException {
        // when
        String alias = fromTemporaryConfiguration().keystoreAlias();

        // then
        assertEquals(alias, "tomcat");
    }

    @Test
    public void shouldHaveEmptyPolicy() throws IOException {
        // when
        String policy = fromTemporaryConfiguration().rccPolicy();

        // then
        assertEquals(policy, "");
    }

    @Test
    public void shouldNotHaveStreamId() throws IOException {
        // when
        String stream = fromTemporaryConfiguration().dMaaPStreamsMapping();

        // then
        assertEquals(stream, null);
    }

    @Test
    public void shouldGetStringDirectly() {
        // given
        String[] cliArguments = {"-param1", "param1value"};

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        String param1value = configurationAccessor.getStringDirectly("param1");

        // then
        assertEquals("param1value", param1value);
    }

    @Test
    public void shouldAddOrUpdate() {
        // given
        String[] cliArguments = {"-param1", "param1value"};

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        configurationAccessor.addOrUpdate("collector.rcc.test", null);
        configurationAccessor.addOrUpdate("collector.rcc.test", "1");
    }

    @Test
    public void shouldControllerConfigFileLocation() throws IOException {
        // given
        String[] cliArguments = {"-param1", "param1value"};

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        String param1value = configurationAccessor.controllerConfigFileLocation();

        // then
        assertTrue(param1value.endsWith("ont_config.json"));
    }

    @Test
    public void shouldConfigurationUpdateFrequency() throws IOException {
        // given
        String[] cliArguments = {"-param1", "param1value"};

        // when
        ApplicationSettings configurationAccessor = new ApplicationSettings(cliArguments, CLIUtils::processCmdLine);
        int param1value = configurationAccessor.configurationUpdateFrequency();

        // then
        assertEquals(5, param1value);
    }

    private static ApplicationSettings fromTemporaryConfiguration(String... fileLines)
        throws IOException {
        File tempConfFile = File.createTempFile("doesNotMatter", "doesNotMatter");
        Files.write(tempConfFile.toPath(), Arrays.asList(fileLines));
        tempConfFile.deleteOnExit();
        return new ApplicationSettings(
            new String[]{"-c", tempConfFile.toString()},
            args -> processCmdLine(args),
            "");
    }

    private String sanitizePath(String path) {
        return Paths.get(path).toString();
    }
}

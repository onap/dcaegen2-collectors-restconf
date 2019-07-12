/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.onap.dcae.TlsTest.HttpsConfiguration.PASSWORD;
import static org.onap.dcae.TlsTest.HttpsConfiguration.USERNAME;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

public class TlsTest extends TlsTestBase {

    @Nested
    @Import(HttpConfiguration.class)
    class HttpTest extends TestClassBase {

        @Test
        public void shouldHttpRequestSucceed() {
            assertEquals(HttpStatus.OK, makeHttpRequest().getStatusCode());
        }

        @Test
        public void shouldHttpsRequestFail() {
            assertThrows(Exception.class, this::makeHttpsRequest);
        }
    }

    @Nested
    @Import(HttpsConfiguration.class)
    class HttpsTest extends TestClassBase {


        @Test
        public void shouldHttpsRequestWithoutBasicAuthFail() {
            assertThrows(Exception.class, this::makeHttpsRequest);
        }

        @Test
        public void shouldHttpsRequestWithBasicAuthSucceed() {
            assertEquals(HttpStatus.OK, makeHttpsRequestWithBasicAuth(USERNAME, PASSWORD).getStatusCode());
        }
    }

    @Nested
    @Import(HttpsConfigurationWithTlsAuthentication.class)
    class HttpsWithTlsAuthenticationTest extends TestClassBase {

        @Test
        public void shouldHttpsRequestWithoutCertificateFail() {
            assertThrows(Exception.class, this::makeHttpsRequest);
        }
    }

    @Nested
    @Import(HttpsConfigurationWithTlsAuthenticationAndBasicAuth.class)
    class HttpsWithTlsAuthenticationAndBasicAuthTest extends TestClassBase {

        @Test
        public void shouldHttpsRequestWithoutBasicAuthFail() {
            assertThrows(Exception.class, this::makeHttpsRequestWithClientCert);
        }

        @Test
        public void shouldHttpsRequestWithBasicAuthSucceed() {
            assertEquals(HttpStatus.OK,
                    makeHttpsRequestWithClientCertAndBasicAuth(USERNAME, PASSWORD).getStatusCode());
        }
    }

    static class HttpConfiguration extends TlsTestBase.ConfigurationBase {
        @Override
        protected void configureSettings(ApplicationSettings settings) {
        }
    }

    static class HttpsConfiguration extends TlsTestBase.ConfigurationBase {
        public static final String USERNAME = "TestUser";
        public static final String PASSWORD = "TestPassword";

        @Override
        protected void configureSettings(ApplicationSettings settings) {
            when(settings.keystoreFileLocation()).thenReturn(KEYSTORE.toString());
            when(settings.keystorePasswordFileLocation()).thenReturn(KEYSTORE_PASSWORD_FILE.toString());
            when(settings.rccKeystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
            when(settings.rccKeystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
            when(settings.authorizationEnabled()).thenReturn(true);
            when(settings.validAuthorizationCredentials()).thenReturn(HashMap.of(USERNAME,
                    "$2a$10$51tDgG2VNLde5E173Ay/YO.Fq.aD.LR2Rp8pY3QAKriOSPswvGviy"));
        }
    }

    static class HttpsConfigurationWithTlsAuthentication extends HttpsConfiguration {
        @Override
        protected void configureSettings(ApplicationSettings settings) {
            super.configureSettings(settings);
            when(settings.authorizationEnabled()).thenReturn(false);
            when(settings.clientTlsAuthenticationEnabled()).thenReturn(true);
            when(settings.truststoreFileLocation()).thenReturn(TRUSTSTORE.toString());
            when(settings.authorizationEnabled()).thenReturn(true);
            when(settings.truststorePasswordFileLocation()).thenReturn(TRUSTSTORE_PASSWORD_FILE.toString());
        }
    }

    static class HttpsConfigurationWithTlsAuthenticationAndBasicAuth extends HttpsConfigurationWithTlsAuthentication {
        @Override
        protected void configureSettings(ApplicationSettings settings) {
            super.configureSettings(settings);
            when(settings.authorizationEnabled()).thenReturn(true);
        }
    }
}

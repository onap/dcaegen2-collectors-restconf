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

import static org.onap.dcae.TestingUtilities.configureKeyStore;
import static org.onap.dcae.TestingUtilities.createRestTemplateWithSsl;
import static org.onap.dcae.TestingUtilities.readFile;
import static org.onap.dcae.TestingUtilities.rethrow;
import static org.onap.dcae.TestingUtilities.sslBuilderWithTrustStore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;





@Configuration
@ExtendWith(SpringExtension.class)
public class TlsTestBase {
    protected static final String KEYSTORE_ALIAS = "tomcat";
    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path KEYSTORE = Paths.get(RESOURCES.toString(), "keystore");
    protected static final Path KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "passwordfile");
    protected static final Path TRUSTSTORE = Paths.get(RESOURCES.toString(), "truststore");
    protected static final Path TRUSTSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "trustpasswordfile");
    protected static final Path RCC_KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "passwordfile");
    protected static final Path RCC_KEYSTORE = Paths.get(RESOURCES.toString(), "keystore");

    protected abstract static class ConfigurationBase {
        protected final ApplicationSettings settings = Mockito.mock(ApplicationSettings.class);

        @Bean
        @Primary
        public ApplicationSettings settings() {
            configureSettings(settings);
            return settings;
        }

        protected abstract void configureSettings(final ApplicationSettings settings);
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    protected abstract class TestClassBase {
        @MockBean
        @Qualifier("inputQueue")
        protected LinkedBlockingQueue<JSONObject> queue;

        @LocalServerPort
        private int port;

        private final String keyStorePassword;
        private final String trustStorePassword;

        public TestClassBase() {
            keyStorePassword = readFile(RCC_KEYSTORE_PASSWORD_FILE);
            trustStorePassword = readFile(TRUSTSTORE_PASSWORD_FILE);
        }

        private String getUrl(final String protocol, final String uri) {
            return protocol + "://localhost:" + port + uri;
        }

        private RestTemplate addBasicAuth(final RestTemplate template, final String username, final String password) {
            template.getInterceptors()
                    .add(new BasicAuthenticationInterceptor(username, password));

            return template;
        }

        public String createHttpUrl(String uri) {
            return getUrl("http", uri);
        }

        public String createHttpsUrl(String uri) {
            return getUrl("https", uri);
        }

        public RestTemplate createHttpRestTemplate() {
            return new RestTemplate();
        }

        public RestTemplate createHttpsRestTemplate() {
            return rethrow(() ->
                    createRestTemplateWithSsl(
                            sslBuilderWithTrustStore(KEYSTORE, keyStorePassword).build()
                    ));
        }

        public RestTemplate createHttpsRestTemplateWithKeyStore() {
            return rethrow(() ->
                    createRestTemplateWithSsl(
                            configureKeyStore(
                                    sslBuilderWithTrustStore(RCC_KEYSTORE, keyStorePassword),
                                    TRUSTSTORE,
                                    trustStorePassword
                            ).build())
            );
        }

        public ResponseEntity<String> makeHttpRequest() {
            return createHttpRestTemplate().getForEntity(createHttpUrl("/"), String.class);
        }

        public ResponseEntity<String> makeHttpsRequest() {
            return createHttpsRestTemplate().getForEntity(createHttpsUrl("/"), String.class);
        }


        public ResponseEntity<String> makeHttpsRequestWithBasicAuth(final String username, final String password) {
            return addBasicAuth(createHttpsRestTemplate(), username, password)
                    .getForEntity(createHttpsUrl("/"), String.class);

        }

        public ResponseEntity<String> makeHttpsRequestWithClientCert() {
            return createHttpsRestTemplateWithKeyStore().getForEntity(createHttpsUrl("/"), String.class);
        }

        public ResponseEntity<String> makeHttpsRequestWithClientCertAndBasicAuth(
                final String username,
                final String password) {
            return addBasicAuth(createHttpsRestTemplateWithKeyStore(), username, password)
                    .getForEntity(createHttpsUrl("/"), String.class);
        }
    }
}

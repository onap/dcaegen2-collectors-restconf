/*
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2018 Nokia. All rights reserved.
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

package org.onap.dcae.restapi;

import java.util.HashMap;
import java.util.Map;
import org.onap.dcae.ApplicationException;
import org.onap.dcae.ApplicationSettings;
import org.onap.dcae.common.AuthMethod;
import org.onap.dcae.common.AuthMethodType;
import org.onap.dcae.common.BasicAuth;
import org.onap.dcae.common.CertAuth;
import org.onap.dcae.common.CertBasicAuth;
import org.onap.dcae.common.NoAuth;
import org.onap.dcae.common.SSLContextCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;

@Component
public class ServletConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private static final Logger log = LoggerFactory.getLogger(ServletConfig.class);

    @Autowired
    private ApplicationSettings properties;

    @Override
    public void customize(ConfigurableServletWebServerFactory container) {
        provideAuthConfigurations(container).getOrDefault(properties.authMethod(),
                notSupportedOperation()).configure();
    }

    private Map<String, AuthMethod> provideAuthConfigurations(ConfigurableServletWebServerFactory container) {
        Map<String, AuthMethod> authMethods = new HashMap<>();
        authMethods.put(AuthMethodType.CERT_ONLY.value(), new CertAuth(container, properties));
        authMethods.put(AuthMethodType.BASIC_AUTH.value(), new BasicAuth(container, properties));
        authMethods.put(AuthMethodType.CERT_BASIC_AUTH.value(), new CertBasicAuth(container, properties));
        authMethods.put(AuthMethodType.NO_AUTH.value(), new NoAuth(container, properties));
        return authMethods;
    }

    private AuthMethod notSupportedOperation() {
        return () -> {
            throw new ApplicationException(
                    "Provided auth method not allowed: " + properties.authMethod());
        };
    }
}
/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
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

package org.onap.dcae.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.onap.dcae.TestingUtilities.assertFailureHasInfo;
import static org.onap.dcae.controller.ConfigSource.getAppConfig;

import io.vavr.control.Try;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.dcae.WiremockBasedTest;


public class ConfigCbsSourceTest extends WiremockBasedTest {

    @Test
    public void shouldReturnValidAppConfiguration() {
        // given
        String sampleConfigForVes = "{\"collector.rcc.service.port\": 8080}";

        stubConsulToReturnLocalAddressOfCbs();
        stubCbsToReturnAppConfig(sampleConfigForVes);

        // when
        Try<JSONObject> actual = tryToGetConfig();

        // then
        assertThat(actual.get().toString()).isEqualTo(new JSONObject(sampleConfigForVes).toString());
    }

    @Test
    public void shouldReturnFailureOnFailureToCommunicateWithConsul() {
        // given
        stubFor(get(urlEqualTo("/v1/catalog/service/CBSName"))
            .willReturn(aResponse().withStatus(400)));

        // when
        Try<JSONObject> actual = tryToGetConfig();

        // then
        assertFailureHasInfo(actual, "HTTP", "Consul", "400",
            "http://localhost:" + wireMockRule.port() + "/v1/catalog/service/CBSName");
    }

    @Test
    public void shouldReturnFailureOnBadJsonFromConsul() {
        // given
        stubFor(get(urlEqualTo("/v1/catalog/service/CBSName"))
            .willReturn(aResponse().withStatus(200).withBody("[{")));

        // when
        Try<JSONObject> actual = tryToGetConfig();

        // then
        assertFailureHasInfo(actual, "JSON", "array", "[{");
    }

    @Test
    public void shouldReturnFailureOnInvalidCatalogFormat() {
        // given
        String notAListCatalog = ""
            + "{"
            + "\"ServiceAddress\":\"localhost\","
            + "\"ServicePort\":" + wireMockRule.port()
            + "}";

        stubFor(get(urlEqualTo("/v1/catalog/service/CBSName"))
            .willReturn(aResponse().withStatus(200).withBody(notAListCatalog)));

        // when
        Try<JSONObject> actual = tryToGetConfig();

        // then
        assertFailureHasInfo(actual, "JSON", "array", notAListCatalog);
    }


    @Test
    public void shouldReturnFailureIfConfigIsMissingRequiredProperties() {
        // given
        String actualConf = "{\"ServicePort\":" + wireMockRule.port() + "}";
        String asCatalog = "[" + actualConf + "]";

        stubFor(get(urlEqualTo("/v1/catalog/service/CBSName"))
            .willReturn(aResponse().withStatus(200).withBody(asCatalog)));

        // when
        Try<JSONObject> actual = tryToGetConfig();

        // then
        assertFailureHasInfo(actual, "ServiceAddress", "ServicePort", "missing", actualConf);
    }


    @Test
    public void shouldReturnFailureOnFailureToCommunicateWithCbs() {
        // given
        stubFor(get(urlEqualTo("/v1/catalog/service/CBSName"))
            .willReturn(aResponse().withStatus(200).withBody(validLocalCbsConf())));
        stubFor(get(urlEqualTo("/service_component/restconfcollector"))
            .willReturn(aResponse().withStatus(400)));

        // when
        Try<JSONObject> actual = tryToGetConfig();

        // then
        assertFailureHasInfo(actual, "HTTP", "CBS", "400",
            "http://localhost:" + wireMockRule.port() + "/service_component/restconfcollector");
    }

    @Test
    public void shouldReturnFailureIfAppIsInvalidJsonDocument() {
        // given
        String invalidAppConf = "[$";
        stubConsulToReturnLocalAddressOfCbs();
        stubCbsToReturnAppConfig(invalidAppConf);

        // when
        Try<JSONObject> actual = tryToGetConfig();

        // then
        assertFailureHasInfo(actual, "JSON", "document", invalidAppConf);
    }

    private Try<JSONObject> tryToGetConfig() {
        return getAppConfig(new EnvProps("http", "localhost", wireMockRule.port(),
                "http", "CBSName", "restconfcollector"));
    }
}


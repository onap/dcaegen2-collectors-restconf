/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2018-2022 Huawei. All rights reserved.
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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;


public class EnvPropsTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            wireMockConfig().dynamicPort().dynamicHttpsPort().keystorePath(null));

    @Test
    public void fromString() {
        assertEquals(new EnvProps("http", "localhost", wireMockRule.port(),
                "http", "CBSName", "restconfcollector")
                .equals(new EnvProps("http", "localhost", wireMockRule.port(),
                        "http", "CBSName", "restconfcollector")), true);
    }

    @Test
    public void fromStringTest() {
        assertEquals(new EnvProps("http", "localhost", wireMockRule.port(),
                "http", "CBSName", "restconfcollector")
                .equals(null), false);
    }

    @Test
    public void fromStringSameObjTest() {
        EnvProps envProps = new EnvProps("http", "localhost", wireMockRule.port(),
                "http", "CBSName", "restconfcollector");
        assertEquals(envProps.equals(envProps), true);
    }

    @Test
    public void toStringTest() {
        String envProps = new EnvProps("http", "localhost", wireMockRule.port(),
                "http", "CBSName", "restconfcollector").toString();
        String str = "EnvProps{consulProtocol='http', consulHost='localhost', consulPort="+wireMockRule.port()+", cbsProtocol='http', cbsName='CBSName', appName='restconfcollector'}";
        assertEquals(envProps, str);
    }

    @Test
    public void hashCodeTest() {
        int envHashCode = new EnvProps("http", "localhost", wireMockRule.port(),
                "http", "CBSName", "restconfcollector").hashCode();
        assertNotNull(envHashCode);
    }
}

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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.onap.dcae.common.Format;


import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

public class EnvPropsTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            wireMockConfig().dynamicPort().dynamicHttpsPort().keystorePath(null));
    @Test
    public void fromString() {
        Assert.assertEquals(new EnvProps("http", "localhost", wireMockRule.port(), "http", "CBSName", "restconfcollector").equals(new EnvProps("http", "localhost", wireMockRule.port(), "http", "CBSName", "restconfcollector")), true);
    }
}
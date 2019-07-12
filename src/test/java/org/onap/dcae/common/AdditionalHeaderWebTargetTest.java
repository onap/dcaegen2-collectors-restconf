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

package org.onap.dcae.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.Silent.class)
public class AdditionalHeaderWebTargetTest {

    @Test
    public void  additionalHaderWebTargettestBase() {
        final Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);

        WebTarget target = mock(WebTarget.class);
        when(target.request()).thenReturn(mockBuilder);
        when(target.request("application/json")).thenReturn(mockBuilder);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
        JSONObject jsonObject = new JSONObject("{\"key\": 1}");
        Map<String, Object> someMap = new HashMap<>();
        someMap.put("id", jsonObject);
        AdditionalHeaderWebTarget webTarget =  new AdditionalHeaderWebTarget(target, "aaa112",
                "someheader");
        webTarget.getConfiguration();
        webTarget.getUri();
        webTarget.getUriBuilder();
        webTarget.path("");
        webTarget.register(AdditionalHeaderWebTarget.class);
        webTarget.register(AdditionalHeaderWebTarget.class,0);
        webTarget.register(AdditionalHeaderWebTarget.class, AdditionalHeaderWebTarget.class);
        Object obj = new Object();
        webTarget.register(obj);
        webTarget.register(obj, 0);
        webTarget.register(obj, AdditionalHeaderWebTarget.class);
        webTarget.request();
        webTarget.request("application/json");
        webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        webTarget.resolveTemplate("key", jsonObject);
        webTarget.resolveTemplate("key", jsonObject, false);
        webTarget.resolveTemplateFromEncoded("key", jsonObject);
        webTarget.resolveTemplates(someMap);
        webTarget.resolveTemplates(someMap, false);
        webTarget.resolveTemplatesFromEncoded(someMap);

        webTarget.matrixParam("key", jsonObject);
        webTarget.queryParam("key", jsonObject);
        webTarget.property("key", jsonObject);
    }
}
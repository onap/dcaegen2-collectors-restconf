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
package org.onap.dcae.common.publishing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.json.JSONObject;
import org.junit.Test;

import io.vavr.control.Option;
import io.vavr.collection.List;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import reactor.core.publisher.Flux;

import static org.junit.Assert.assertEquals;
import static org.onap.dcae.common.publishing.DmaapRequestConfiguration.createPublishRequest;
import static org.onap.dcae.common.publishing.DmaapRequestConfiguration.retryConfiguration;
import static org.onap.dcae.common.publishing.DmaapRequestConfiguration.jsonBatch;
import static org.onap.dcae.common.publishing.DmaapRequestConfiguration.createMessageRouterSink;
import static org.onap.dcae.common.publishing.DmaapRequestConfiguration.getAsJsonElements;


public class DmaapRequestConfigurationTest {

    private static final Long TIMEOUT_SECONDS = 10L;

    @Test
    public void createPublishRequestTest () {
        List<String> list = List.of("test");
        PublisherConfig publisherConfig = new PublisherConfig(list, "topic");
        Option<PublisherConfig> pb = Option.of(publisherConfig);
        createPublishRequest(pb);
    }

    @Test
    public void createPublishRequest2Test () {
        List<String> list = List.of("test");
        PublisherConfig publisherConfig = new PublisherConfig(list, "topic");
        Option<PublisherConfig> pb = Option.of(publisherConfig);
        createPublishRequest(pb, TIMEOUT_SECONDS);
    }

    @Test
    public void retryConfigurationTest () {
        retryConfiguration();
    }

    @Test
    public void jsonBatchTest () {
        JSONObject obj = new JSONObject();
        obj.put("message", "hello world!!!");
        List<String> listStr = List.of("{message: 'Hello World!!!'}");
        Flux<JsonObject> fjso = jsonBatch(listStr);
    }

    @Test
    public void createMessageRouterSinkTest () {
        ImmutableMessageRouterSink imrs = createMessageRouterSink("testUrl");
        assertEquals("testUrl", imrs.topicUrl());
    }

    @Test
    public void getAsJsonElementsTest () {
        List<String> listStr = List.of("test_message");
        List<JsonElement> jsList = getAsJsonElements(listStr);
        JsonElement element = new JsonPrimitive("test_message");
        assertEquals(element, jsList.get(0));
    }
}

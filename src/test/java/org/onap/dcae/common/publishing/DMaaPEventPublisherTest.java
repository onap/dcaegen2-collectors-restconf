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

import io.vavr.API;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;


public class DMaaPEventPublisherTest {

    private Map<String, PublisherConfig> dmaapconfigs;
    /**
     * Setup before test.
     */
    @Before
    public void setUp() {
        dmaapconfigs = API.Map("sampleStream1", new PublisherConfig(API.List("destination1"), "topic1"));
    }

    @Test
    public void reconfigureEventTest () throws Exception {
        List<String> destination = List.of("test");
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        Map<String, PublisherConfig> publisherCache = HashMap.of("tester", publisherConfig);
        publisherCache.put("tester", publisherConfig);
        DMaaPPublishersCache dmaapPublishersCache = new DMaaPPublishersCache(dmaapconfigs);
        DMaaPEventPublisher dMaaPEventPublisher = new DMaaPEventPublisher(publisherCache);
        dMaaPEventPublisher.reconfigure(publisherCache);
        assertEquals("Test", publisherCache.get()._2.topic());
    }

    @Test
    public void sendEventTest () throws Exception {
        List<String> destination = List.of("test");
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        DMaaPPublishersCache.CambriaPublishersCacheLoader publishersCache =
                Mockito.mock(DMaaPPublishersCache.CambriaPublishersCacheLoader.class);
        Map<String, PublisherConfig> publisherCache = HashMap.of("test", publisherConfig);
        publisherCache.put("test", publisherConfig);
        DMaaPEventPublisher dMaaPEventPublisher = new DMaaPEventPublisher(publisherCache);
        JSONObject jo = new JSONObject();
        jo.put("test", "message");
        dMaaPEventPublisher.sendEvent(jo,"test");
        assertEquals("test", publisherCache.get()._1);
    }
}

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

import io.vavr.collection.List;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PublisherTest {

    @Mock
    DmaapRequestConfiguration dmaapRequestConfiguration;

    @Mock
    MessageRouterPublishRequest messageRouterPublishRequest;

    @Mock
    MessageRouterPublisherConfig messageRouterPublisherConfig;

    @Test
    public void publishEvents () throws Exception {

        JSONObject jo = new JSONObject();
        jo.put("message", "test");
        List<String> event = List.of(jo.toString());
        Publisher publisher = new Publisher(messageRouterPublisherConfig);
    }
}

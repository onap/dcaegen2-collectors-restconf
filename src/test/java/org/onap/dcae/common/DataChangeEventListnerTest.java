/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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


import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import org.onap.dcae.RestConfCollector;
import org.onap.dcae.common.publishing.DMaaPConfigurationParser;
import org.onap.dcae.common.publishing.EventPublisher;


import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;

@RunWith(MockitoJUnitRunner.class)
public class DataChangeEventListnerTest {
    @Mock
    InboundEvent event;

    @Test
    public void testDataChangeEventListenerString() {
        when(event.readData()).thenReturn("");
        DataChangeEventListener listner = new DataChangeEventListener(null);
        listner.onEvent(event);
    }

    @Test
    public void testDataChangeEventListenerJsonObject() {
        when(event.readData()).thenReturn("{\"name\":\"john\",\"age\":22,\"class\":\"mca\"}");
        RestConfCollector.fProcessingInputQueue = new LinkedBlockingQueue<>(4);
        DataChangeEventListener listner = new DataChangeEventListener(null);
        listner.onEvent(event);
    }
    @Test
    public void testDataChangeEventListenerJsonArray() {
        when(event.readData()).thenReturn("[{ \"name\":\"Ford\", \"models\":[ \"Fiesta\",\"Focus\", \"Mustang\" ] },{\"name\":\"BMW\", \"models\":[ \"320\", \"X3\",\"X5\" ] },{\"name\":\"Fiat\",\"models\":[ \"500\", \"Panda\" ]}]");
        RestConfCollector.fProcessingInputQueue = new LinkedBlockingQueue<>(4);
        DataChangeEventListener listner = new DataChangeEventListener(null);
        listner.onEvent(event);
    }
}
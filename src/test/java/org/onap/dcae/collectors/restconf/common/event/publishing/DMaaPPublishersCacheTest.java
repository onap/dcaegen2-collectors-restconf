/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
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

package org.onap.dcae.collectors.restconf.common.event.publishing;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.junit.Before;
import org.junit.Test;
import org.onap.dcae.collectors.restconf.common.event.publishing.DMaaPPublishersCache.OnPublisherRemovalListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static io.vavr.API.List;
import static io.vavr.API.Map;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DMaaPPublishersCacheTest {

    private String streamId1;
    private Map<String, PublisherConfig> dMaaPConfigs;

    @Before
    public void setUp() {
        streamId1 = "sampleStream1";
        dMaaPConfigs = Map("sampleStream1", new PublisherConfig(List("destination1"), "topic1"));
    }

    @Test
    public void shouldReturnTheSameCachedInstanceOnConsecutiveRetrievals() {
        // given
        DMaaPPublishersCache dMaaPPublishersCache = new DMaaPPublishersCache(dMaaPConfigs);

        // when
        Option<CambriaBatchingPublisher> firstPublisher = dMaaPPublishersCache.getPublisher(streamId1);
        Option<CambriaBatchingPublisher> secondPublisher = dMaaPPublishersCache.getPublisher(streamId1);

        // then
        assertSame("should return same instance", firstPublisher.get(), secondPublisher.get());
    }

    @Test
    public void shouldCloseCambriaPublisherOnCacheInvalidate() throws IOException, InterruptedException {
        // given
        CambriaBatchingPublisher cambriaPublisherMock1 = mock(CambriaBatchingPublisher.class);
        DMaaPPublishersCache.CambriaPublishersCacheLoader cacheLoaderMock = mock(DMaaPPublishersCache.CambriaPublishersCacheLoader.class);
        DMaaPPublishersCache dMaaPPublishersCache = new DMaaPPublishersCache(cacheLoaderMock,
                                                                             new OnPublisherRemovalListener(),
                                                                             dMaaPConfigs);
        when(cacheLoaderMock.load(streamId1)).thenReturn(cambriaPublisherMock1);

        // when
        dMaaPPublishersCache.getPublisher(streamId1);
        dMaaPPublishersCache.closePublisherFor(streamId1);

        // then
        verify(cambriaPublisherMock1).close(20, TimeUnit.SECONDS);

    }

    @Test
    public void shouldReturnNoneIfThereIsNoDMaaPConfigurationForGivenStreamID() {
        // given
        DMaaPPublishersCache dMaaPPublishersCache = new DMaaPPublishersCache(dMaaPConfigs);

        // then
        assertTrue("should not exist", dMaaPPublishersCache.getPublisher("non-existing").isEmpty());
    }

}
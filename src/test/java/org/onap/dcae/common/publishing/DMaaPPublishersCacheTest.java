/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
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

package org.onap.dcae.common.publishing;

import static io.vavr.API.List;
import static io.vavr.API.Map;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.onap.dcae.common.publishing.DMaaPPublishersCache.CambriaPublishersCacheLoader;
import org.onap.dcae.common.publishing.DMaaPPublishersCache.OnPublisherRemovalListener;


public class DMaaPPublishersCacheTest {

    private String streamId1;
    private Map<String, PublisherConfig> dmaapconfigs;
    private Map<String, PublisherConfig> dmaapconfigs2;
    /**
     * Setup before test.
     */
    @Before
    public void setUp() {
        streamId1 = "sampleStream1";
        dmaapconfigs = Map("sampleStream1", new PublisherConfig(List("destination1"), "topic1"));
        dmaapconfigs2 = Map("sampleStream1", new PublisherConfig(List("destination1"),
                "topic1", "user", "pass"));

    }

    @Test
    public void shouldReturnTheSameCachedInstanceOnConsecutiveRetrievals() {
        // given
        DMaaPPublishersCache dmaapPublishersCache = new DMaaPPublishersCache(dmaapconfigs);

        // when
        Option<CambriaBatchingPublisher> firstPublisher = dmaapPublishersCache.getPublisher(streamId1);
        Option<CambriaBatchingPublisher> secondPublisher = dmaapPublishersCache.getPublisher(streamId1);

        // then
        assertSame("should return same instance", firstPublisher.get(), secondPublisher.get());
    }

    @Test
    public void shouldReturnTheSameCachedInstanceOnConsecutiveRetrievals2() {
        // given
        DMaaPPublishersCache dmaapPublishersCache = new DMaaPPublishersCache(dmaapconfigs2);

        // when
        Option<CambriaBatchingPublisher> firstPublisher = dmaapPublishersCache.getPublisher(streamId1);
        Option<CambriaBatchingPublisher> secondPublisher = dmaapPublishersCache.getPublisher(streamId1);

        // then
        assertSame("should return same instance", firstPublisher.get(), secondPublisher.get());
    }

    @Test
    public void shouldCloseCambriaPublisherOnCacheInvalidate() throws IOException, InterruptedException {
        // given
        CambriaBatchingPublisher cambriaPublisherMock1 = mock(CambriaBatchingPublisher.class);
        CambriaPublishersCacheLoader cacheLoaderMock = mock(CambriaPublishersCacheLoader.class);
        DMaaPPublishersCache dmaapPublishersCache = new DMaaPPublishersCache(cacheLoaderMock,
                                                                             new OnPublisherRemovalListener(),
                dmaapconfigs);
        when(cacheLoaderMock.load(streamId1)).thenReturn(cambriaPublisherMock1);

        // when
        dmaapPublishersCache.getPublisher(streamId1);
        dmaapPublishersCache.closePublisherFor(streamId1);

        // then
        verify(cambriaPublisherMock1).close(20, TimeUnit.SECONDS);

    }

    @Test
    public void shouldReturnNoneIfThereIsNoDmaaPConfigurationForGivenStreamId() {
        // given
        DMaaPPublishersCache dmaapPublishersCache = new DMaaPPublishersCache(dmaapconfigs);

        // then
        assertTrue("should not exist", dmaapPublishersCache.getPublisher("non-existing").isEmpty());
    }


    @Test
    public void shouldCloseOnlyChangedPublishers() throws IOException, InterruptedException {
        // given
        CambriaBatchingPublisher cambriaPublisherMock1 = mock(CambriaBatchingPublisher.class);
        CambriaBatchingPublisher cambriaPublisherMock2 = mock(CambriaBatchingPublisher.class);
        CambriaPublishersCacheLoader cacheLoaderMock = mock(CambriaPublishersCacheLoader.class);
        String firstDomain = "domain1";
        String secondDomain = "domain2";
        Map<String, PublisherConfig> oldConfig = Map(firstDomain,
                                                     new PublisherConfig(List("destination1"), "topic1"),
                                                     secondDomain,
                                                     new PublisherConfig(List("destination2"), "topic2",
                                                                         "user", "pass"));

        DMaaPPublishersCache dmaapPublishersCache = new DMaaPPublishersCache(cacheLoaderMock,
                                                                             new OnPublisherRemovalListener(),
                                                                             oldConfig);
        final Map<String, PublisherConfig> newConfig = Map(firstDomain,
                new PublisherConfig(List("destination1"), "topic1"),
                secondDomain, new PublisherConfig(List("destination2"), "topic2"));

        when(cacheLoaderMock.load(firstDomain)).thenReturn(cambriaPublisherMock1);
        when(cacheLoaderMock.load(secondDomain)).thenReturn(cambriaPublisherMock2);

        dmaapPublishersCache.getPublisher(firstDomain);
        dmaapPublishersCache.getPublisher(secondDomain);

        // when
        dmaapPublishersCache.reconfigure(newConfig);

        // then
        verify(cambriaPublisherMock2).close(20, TimeUnit.SECONDS);
        verifyZeroInteractions(cambriaPublisherMock1);
    }
}
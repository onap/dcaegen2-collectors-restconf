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
import io.vavr.control.Option;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PublisherConfigTest {
    List<String> destination = List.of("test");
    @Test
    public void isSecuredTest () {
        destination.append("test");
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        boolean isSecure = publisherConfig.isSecured();
        assertEquals(true, isSecure);
    }

    @Test
    public void hashCodeTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        publisherConfig.hashCode();
    }

    @Test
    public void topicTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "topic");
        String topic = publisherConfig.topic();
        assertEquals("topic", topic);
    }

    @Test
    public void usernameTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        Option<String> username = publisherConfig.userName();
        assertEquals("test", username.get());
    }

    @Test
    public void passwordTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        Option<String> password = publisherConfig.password();
        assertEquals("test123", password.get());
    }

    @Test
    public void destinationTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        List<String> dest = publisherConfig.destinations();
        assertEquals(destination, dest);
    }

    @Test
    public void getHostNPortTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        String hostAndPort = publisherConfig.getHostAndPort();
        assertEquals("test", hostAndPort);
    }

    @Test
    public void isEqualTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "topic", "test",
                "test123");
        boolean isEqual1 = publisherConfig.equals(publisherConfig);
        boolean isEqual2 = publisherConfig.equals(null);
        assertEquals(true, isEqual1);
    }

    @Test
    public void toStringTest () {
        PublisherConfig publisherConfig = new PublisherConfig(destination, "Test", "test",
                "test123");
        String str = publisherConfig.toString();
        assertEquals("PublisherConfig{destinations=List(test), topic='Test', userName='test', " +
                        "password='test123'}", str);
    }
}

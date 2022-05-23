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
package org.onap.dcae;

import org.junit.Test;

public class RestConfCollectorTest {

    @Test
    public void restartApplicationTest () throws Exception {
        RestConfCollector restConfCollector = new RestConfCollector();
        restConfCollector.restartApplication();
    }

    @Test
    public void applicationSettingsTest () throws Exception {
        RestConfCollector restConfCollector = new RestConfCollector();
        restConfCollector.applicationSettings();
    }

    @Test
    public void inputQueueTest () throws Exception {
        RestConfCollector restConfCollector = new RestConfCollector();
        restConfCollector.inputQueue();
    }

    @Test
    public void parseStreamIdToStreamHashMappingTest () throws Exception {
        RestConfCollector restConfCollector = new RestConfCollector();
        restConfCollector.parseStreamIdToStreamHashMapping("SampleStream1");
    }
}

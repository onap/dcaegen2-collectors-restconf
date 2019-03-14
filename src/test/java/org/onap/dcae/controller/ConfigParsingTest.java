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

package org.onap.dcae.controller;


import static io.vavr.API.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcae.TestingUtilities.assertJSONObjectsEqual;
import static org.onap.dcae.TestingUtilities.readJSONFromFile;

import io.vavr.collection.Map;
import io.vavr.control.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConfigParsingTest {

    @Test
    public void shouldReturnDMaaPConfig() {
        JSONObject dMaaPConf = new JSONObject("{\"auth-credentials-present\": {\"aaf_username\": \"sampleUser\",\"dmaap_info\": {\"topic_url\": \"http://UEBHOST:3904/events/DCAE-SE-COLLECTOR-EVENTS-DEV\",},\"aaf_password\": \"samplePassword\"}}");
        JSONObject root = new JSONObject();
        root.put("key1", "someProperty");
        root.put("key2", "someProperty");
        root.put("streams_publishes", dMaaPConf);

        Option<JSONObject> dMaaPConfig = ConfigParsing.getDMaaPConfig(root);

        assertThat(dMaaPConfig.isEmpty()).isFalse();
        assertJSONObjectsEqual(dMaaPConfig.get(), dMaaPConf);
    }

    @Test
    public void shouldReturnEmptyIfDMaaPConfigIsInvalid() {
        JSONObject root = new JSONObject();
        root.put("streams_publishes", 1);

        Option<JSONObject> dMaaPConfig = ConfigParsing.getDMaaPConfig(root);

        assertThat(dMaaPConfig.isEmpty()).isTrue();
    }

    @Test
    public void getProperties() {
        JSONObject dMaaPConf = new JSONObject("{\"auth-credentials-present\": {\"aaf_username\": \"sampleUser\",\"dmaap_info\": {\"topic_url\": \"http://UEBHOST:3904/events/DCAE-SE-COLLECTOR-EVENTS-DEV\",},\"aaf_password\": \"samplePassword\"}}");
        JSONObject root = new JSONObject();
        root.put("key1", "someProperty");
        root.put("key2", "someProperty");
        root.put("streams_publishes", dMaaPConf);

        Map<String, String> properties = ConfigParsing.getProperties(root);
        assertThat(properties).isEqualTo(Map("key1", "someProperty", "key2", "someProperty"));
    }
}
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

package org.onap.dcae.common.publishing;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Test;
import org.onap.dcae.common.JsonParser;

public class JsonParserTest {

    @Test
    public void convertToPropertiesTest() throws Exception {
        String testJson = "{\"prop2\"=\"value\", \"prop1\"=\"value\"}";
        Map<String, String> response = JsonParser.convertToProperties(testJson);
        assertEquals("value", response.get("prop2"));
    }

    @Test
    public void convertToPropertiesTestwithArray() throws Exception {
        String testJson = "{\"metadatum\": [{\"metaname\": \"remote-id\",\"metaval\": \"AC9.0234.0337\","
                + "\"resource-version\": \"1553802421110\"},{\"metaname\": \"svlan\",\"metaval\": \"100\","
                + "\"resource-version\": \"1553802421082\"}]}";
        Map<String, String> response = JsonParser.convertToProperties(testJson);
        assertEquals("100", response.get("metadatum[1].metaval"));
    }
}

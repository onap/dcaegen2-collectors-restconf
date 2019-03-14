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

package org.onap.dcae.common;

import org.json.XML;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class XmlJsonUtilTest {

    @Test
    public void getJsonOrXmlTest() {
        String var = "result";
        Map<String, String> mm = new HashMap<>();


        mm.put("result.time", "2018 12:04");
        mm.put("result.status", "200");
        mm.put("result.output", "xml2json");
        mm.put("result.[", "start");
        mm.put("result.]", "end");
        try {
            String str = XmlJsonUtil.getXml(mm, var);
            assertEquals(str.startsWith("<"), true);
            String str2 = XmlJsonUtil.getJson(mm, var);
            assertEquals(str2.startsWith("{"), true);
        }catch (Exception e) {}
    }

    @Test
    public void removeEmptystructFromXml() {
        String var = "<time>2018 12:04</time>\n" +
                "<output>t2</output>\n" +
                "<start>bad\n" +
                "<status>200</status>";
        Map<String, String> mm = new HashMap<>();
        try {
            String str = XmlJsonUtil.removeEmptyStructXml(var);

        }catch (Exception e) {}
    }

    @Test
    public void removeEmptystructFromJson() {
        String var = "{\n" +
                "\t\"output\": \"xml2json\",\n" +
                "\t\"time\": \"2018 12:04\",\n" +
                "\t\"\": \"end\",\n" +
                "\t\"status\": \"200\"\n" +
                "}";
        Map<String, String> mm = new HashMap<>();

        try {
            String str = XmlJsonUtil.removeEmptyStructJson(var);
        }catch (Exception e) {}
    }
}
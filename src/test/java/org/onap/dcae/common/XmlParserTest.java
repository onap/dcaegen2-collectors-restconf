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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class XmlParserTest {

    @Test
    public void setAttribute() {
        String convert =  "<time>2018 12:04</time>\n"
                + "<output>t2</output>\n"
                + "<status>200</status>";
        Set<String> listNameList = new HashSet<>();
        listNameList.add("result");
        Map<String, String> propMap;
        try {
            propMap = XmlParser.convertToProperties(convert, listNameList);
            System.out.println(propMap);
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
    }
}
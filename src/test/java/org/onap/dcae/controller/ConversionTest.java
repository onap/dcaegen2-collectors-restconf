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
package org.onap.dcae.controller;

import io.vavr.collection.List;
import io.vavr.control.Try;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConversionTest implements Conversions {
    @Test
    public void toJsonTest () throws Exception {
        Try<JSONObject> jo = Conversions.toJson("{\"name\":\"Test\",\"description\":\"description\"}");
        jo.onSuccess(value -> assertEquals(value.get("name"), "Test"));
    }

    @Test
    public void toJsonArrayTest () throws Exception {
        Try<JSONArray> jo = Conversions.toJsonArray("[{\"name\":\"Test\",\"description\":\"description\"}]");
        jo.onSuccess(value -> assertTrue(value.get(0).toString().contains("Test")));
    }

    @Test
    public void toListTest () throws Exception {
        ArrayList<String> al = new ArrayList<String>();
        al.add("Test");
        Iterator<String> itr = al.iterator();
        List<String> jo = Conversions.toList(itr);
        assertEquals(jo.get(0), "Test");
    }
}

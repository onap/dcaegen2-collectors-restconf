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

package org.onap.dcae.collectors.restconf.restconftest;

import com.att.nsa.cmdLine.NsaCommandLineUtil;
import com.att.nsa.drumlin.till.nv.impl.nvReadableStack;
import com.att.nsa.drumlin.till.nv.impl.nvReadableTable;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.dcae.collectors.restconf.common.Constants;
import org.onap.dcae.collectors.restconf.common.RestConfProc;

import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;

public class TestRestConfCollector {
    @Test
    public void testParseCLIArguments() {
        // given
        String args[] = {"-a", "aa"};
        Map<String, String> argMap = NsaCommandLineUtil.processCmdLine(args, true);
        // when
        nvReadableStack settings = new nvReadableStack();
        settings.push(new nvReadableTable(argMap));

        // then
        assertEquals(settings.getString("a", "default"), "aa");
    }

    @Test
    public void shouldPutValidRestConfEventOnProcessingQueueWithoutExceptions() throws Exception {
        // given
        RestConfProc.fProcessingInputQueue = new LinkedBlockingQueue<>(
                Constants.KDEFAULT_MAXQUEUEDEVENTS);
        JsonElement restConfEvent = new JsonParser().parse(new FileReader("src/test/resources/RestConfEvent.json"));
        JSONObject validRestConfEvent = new JSONObject(restConfEvent.toString());
        JSONArray jsonArrayMod = new JSONArray().put(validRestConfEvent);

        // then
        RestConfProc.handleEvents(jsonArrayMod);
    }
}

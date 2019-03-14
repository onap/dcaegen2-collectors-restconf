/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
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

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.dcae.RestConfCollector;
import org.onap.dcae.controller.PersistentEventConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataChangeEventListener implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(DataChangeEventListener.class);
    private PersistentEventConnection conn;

    public DataChangeEventListener(PersistentEventConnection conn) {
        this.conn = conn;
    }

    @Override
    public void onEvent(InboundEvent event) {
        try {
            log.info("SSE Event is received");
            String s = event.readData();
            jsonType type = isJSONValid(s);
            if (type == jsonType.OBJECT) {
                JSONObject jsonObj = new JSONObject(s);
                EventData ev = new EventData(this.conn, jsonObj);
                log.info("SSE Event in json " + jsonObj.toString());
                RestConfCollector.handleEvents(ev);
            } else if (type == jsonType.ARRAY) {
                JSONArray jsonArr = new JSONArray(s);
                for (int j = 0; j < jsonArr.length(); j++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(j);
                    EventData ev = new EventData(this.conn, jsonObj);
                    log.info("SSE Event in json " + jsonObj.toString());
                    RestConfCollector.handleEvents(ev);
                }
            } else {
                log.info("Received heart beat ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private enum jsonType {
        OBJECT, ARRAY, NONE;
    }

    public jsonType isJSONValid(String test) {
        try {
            new JSONObject(test);
            log.info("Received a Json object");
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
                return jsonType.ARRAY;
            } catch (JSONException ex1) {
                return jsonType.NONE;
            }
        }
        return jsonType.OBJECT;
    }
}

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

package org.onap.dcae.collectors.restconf.common;

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataChangeEventListener implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(DataChangeEventListener.class);
    private RestConfContext ctx;

    public DataChangeEventListener(RestConfContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onEvent(InboundEvent event) {
        JSONArray jsonArrayMod;
        log.info("On SSE Event is received");
        String s = event.readData();
        JSONObject jsonObj = new JSONObject(s);
        jsonArrayMod = new JSONArray().put(jsonObj);
        try {
            RestConfProc.handleEvents(jsonArrayMod);
        } catch (Exception e) {
           log.error("Error occured in DataChangeEventListener.onEvent"+e);
        }
    }
}

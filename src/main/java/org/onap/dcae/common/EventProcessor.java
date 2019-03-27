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


import org.json.JSONException;
import org.json.JSONObject;
import org.onap.dcae.RestConfCollector;
import org.onap.dcae.common.publishing.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EventProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private Map<String, String[]> streamidHash = new HashMap<>();

    private EventPublisher eventPublisher;

    public EventProcessor(EventPublisher eventPublisher, Map<String, String[]> streamidHash) {
        this.eventPublisher = eventPublisher;
        this.streamidHash.putAll(streamidHash);
    }


    @Override
    public void run() {
        try {
            EventData ev;
            while (true) {
                ev = RestConfCollector.fProcessingInputQueue.take();

                // As long as the producer is running we remove elements from
                // the queue.
                log.info("QueueSize:" + RestConfCollector.fProcessingInputQueue.size() + "\tEventProcessor\tRemoving element: " +
                        ev.getEventObj());
                /*@TODO: Right now all event publish to single domain and consume by VES collector. Later maybe send to specific domain */
                String[] streamIdList = streamidHash.get("notification");
                log.info("streamIdList:" + Arrays.toString(streamIdList));

                if (streamIdList.length == 0) {
                    log.error("No StreamID defined for publish - Message dropped" + ev.getEventObj());
                } else {
                    sendEventsToStreams(streamIdList, ev);
                }
                log.info("Event published" + ev.getEventObj());
            }
        } catch (Exception e) {
            log.error("EventProcessor InterruptedException" + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void sendEventsToStreams(String[] streamIdList, EventData ev) {
        for (String aStreamIdList : streamIdList) {
            log.info("Invoking publisher for streamId:" + aStreamIdList);
            if (!ev.getConn().getEventRuleId().equals("")) {
                JSONObject customHeader = new JSONObject();
                customHeader.put("rule-id", ev.getConn().getEventRuleId());
                eventPublisher.sendEvent(overrideEvent(customHeader, ev.getEventObj()), aStreamIdList);
            } else {
                eventPublisher.sendEvent(ev.getEventObj(), aStreamIdList);
            }
        }
    }

    private static JSONObject overrideEvent(JSONObject json1, JSONObject json2) {
        JSONObject mergedJSON;
        try {
            mergedJSON = new JSONObject(json1, JSONObject.getNames(json1));
            for (String key : JSONObject.getNames(json2)) {
                mergedJSON.put(key, json2.get(key));
            }

        } catch (JSONException e) {
            throw new RuntimeException("JSON Exception" + e);
        }
        log.info("Merged json " + mergedJSON);
        return mergedJSON;
    }
}

/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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


import org.json.JSONObject;
import org.onap.dcae.collectors.restconf.common.event.publishing.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EventProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    static Map<String, String[]> streamidHash = new HashMap<>();
    public JSONObject event;
    private EventPublisher eventPublisher;

    public EventProcessor(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        streamidHash = parseStreamIdToStreamHashMapping(new RestConfProc().streamID);
    }

    private Map<String, String[]> parseStreamIdToStreamHashMapping(String streamId) {
        Map<String, String[]> streamidHash = new HashMap<>();
        String[] list = streamId.split("\\|");
        for (String aList : list) {
            String domain = aList.split("=")[0];
            String[] streamIdList = aList.substring(aList.indexOf('=') + 1).split(",");
            streamidHash.put(domain, streamIdList);
        }
        return streamidHash;
    }

    @Override
    public void run() {
        try {

            while (true) {
                event = RestConfProc.fProcessingInputQueue.take();
                // As long as the producer is running we remove elements from
                // the queue.
                log.info("QueueSize:" + RestConfProc.fProcessingInputQueue.size() + "\tEventProcessor\tRemoving element: " +
                                 event);
                String[] streamIdList = streamidHash.get("route");
                log.debug("streamIdList:" + Arrays.toString(streamIdList));

                if (streamIdList.length == 0) {
                    log.error("No StreamID defined for publish - Message dropped" + event);
                } else {
                    sendEventsToStreams(streamIdList);
                }
                log.debug("Event published" + event);
            }
        } catch (Exception e) {
            log.error("EventProcessor InterruptedException" + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void sendEventsToStreams(String[] streamIdList) {
        for (String aStreamIdList : streamIdList) {
            log.info("Invoking publisher for streamId:" + aStreamIdList);
            eventPublisher.sendEvent(event, aStreamIdList);
        }
    }
}

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


import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.dcae.collectors.restconf.common.event.publishing.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    static Map<String, String[]> streamidHash = new HashMap<>();
    public JSONObject event;
    private JSONObject vesEvent;
    private EventPublisher eventPublisher;
    private String target;
    private long eventTime;

    public EventProcessor(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        streamidHash = parseStreamIdToStreamHashMapping(RestConfProc.streamID);
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
                vesEvent = parseJsonToVESEvent(event);
                // As long as the producer is running we remove elements from
                // the queue.
                log.info("QueueSize:" + RestConfProc.fProcessingInputQueue.size() + "\tEventProcessor\tRemoving element: " +
                                 vesEvent);
                String[] streamIdList = streamidHash.get("fault");
                log.debug("streamIdList:" + Arrays.toString(streamIdList));

                if (streamIdList.length == 0) {
                    log.error("No StreamID defined for publish - Message dropped" + vesEvent);
                } else {
                    sendEventsToStreams(streamIdList);
                }
                log.debug("Event published" + vesEvent);
            }
        } catch (Exception e) {
            log.error("EventProcessor InterruptedException" + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void sendEventsToStreams(String[] streamIdList) {
        for (String aStreamIdList : streamIdList) {
            log.info("Invoking publisher for streamId:" + aStreamIdList);
            eventPublisher.sendEvent(vesEvent, aStreamIdList);
        }
    }

    private JSONObject parseJsonToVESEvent(JSONObject jsonNode) {
        JSONObject vesEvent = new JSONObject();
        JSONObject mergeObject = new JSONObject();
        JSONObject restConfNotf = jsonNode.getJSONObject("ietf-restconf:notification");
        JSONObject yangPatch = restConfNotf.getJSONObject("ietf-yang-push:push-change-update").getJSONObject
                ("datastore-changes").getJSONObject("ietf-yang-patch:yang-patch").getJSONArray("edit")
                .getJSONObject(0);
        target = yangPatch.get("target").toString();
        eventTime = parseEventTimeIntoMicroSeconds(restConfNotf.get("eventTime").toString());
        JSONObject commonEventHeader = updateCommonEventHeader();
        JSONObject faultFields = updateFaultFields(yangPatch);
        mergeObject.put("commonEventHeader", commonEventHeader);
        mergeObject.put("faultFields", faultFields);
        vesEvent.put("event", mergeObject);
        System.out.println(vesEvent);
        return vesEvent;
    }

    private JSONObject updateCommonEventHeader() {
        JSONObject commonEventHeader = new JSONObject();
        final UUID uuid = UUID.randomUUID();
        commonEventHeader.put("sourceId", target);
        commonEventHeader.put("startEpochMicrosec", eventTime);
        commonEventHeader.put("eventId", uuid);
        commonEventHeader.put("domain", "fault");
        commonEventHeader.put("lastEpochMicrosec", eventTime);
        commonEventHeader.put("eventName", "Fault_Route_Status");
        commonEventHeader.put("sourceName", target);
        commonEventHeader.put("priority", "High");
        commonEventHeader.put("version", 3.0);
        commonEventHeader.put("reportingEntityName", "Domain_Contorller");
        return commonEventHeader;
    }

    private JSONObject updateFaultFields(JSONObject yangPatch) {
        JSONObject faultFields = new JSONObject();
        JSONObject networkTopo = yangPatch.getJSONObject("value")
                .getJSONArray("ietf-network-topology:termination-point").getJSONObject(0);
        faultFields.put("eventSeverity", "CRITICAL");
        faultFields.put("alarmCondition", "Route_Status");
        faultFields.put("faultFieldsVersion", 2.0);
        faultFields.put("specificProblem", "Fault_SOTN_Service_Status");
        faultFields.put("eventSourceType", "other");
        faultFields.put("vfStatus", "Active");
        JSONArray alarmInfo = parseAlarmAdditionalInformation(networkTopo);
        faultFields.put("alarmAdditionalInformation", alarmInfo);
        return faultFields;
    }

    private JSONArray parseAlarmAdditionalInformation(JSONObject networkTopo) {
        JSONArray alarmInfo = new JSONArray();
        JSONObject termEndPoint = networkTopo.getJSONObject("ietf-te-topology:te");
        JSONObject supTermPoint = networkTopo.getJSONArray("supporting-termination-point").getJSONObject(0);
        String[] targetInformation = parseTargetInformation(target);
        alarmInfo.put(getAlarmAdditionalInformation("networkId",
                                                    targetInformation[0]));
        alarmInfo.put(getAlarmAdditionalInformation("node", targetInformation[1]));
        alarmInfo.put(getAlarmAdditionalInformation("tp-id", networkTopo.get("tp-id").toString()));
        alarmInfo.put(getAlarmAdditionalInformation("oper-status", termEndPoint.get("oper-status").toString()));
        alarmInfo.put(getAlarmAdditionalInformation("inter-domain-plug-id",
                                                    termEndPoint.get("inter-domain-plug-id").toString()));
        alarmInfo.put(getAlarmAdditionalInformation("network-ref", supTermPoint.get("network-ref").toString()));
        alarmInfo.put(getAlarmAdditionalInformation("node-ref", supTermPoint.get("node-ref").toString()));
        alarmInfo.put(getAlarmAdditionalInformation("tp-ref", supTermPoint.get("tp-ref").toString()));
        return alarmInfo;
    }

    private JSONObject getAlarmAdditionalInformation(String name, String value) {
        JSONObject alarmInfo = new JSONObject();
        alarmInfo.put("name", name);
        alarmInfo.put("value", value);
        return alarmInfo;
    }

    private String[] parseTargetInformation(String target) {
        String[] targetInfo = new String[2];
        String[] couple = target.split("/");
        for (String aCouple : couple) {
            String[] split = aCouple.split("=");
            for (int j = 0; j < split.length; j++) {
                if (split[j].equals("network")) {
                    targetInfo[0] = split[j + 1];
                }
                if (split[j].equals("node")) {
                    targetInfo[1] = split[j + 1];
                }
            }
        }
        return targetInfo;
    }

    private long parseEventTimeIntoMicroSeconds(String eventTime) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            //formatting the dateString to convert it into a Date
            date = simpleDateFormat.parse(eventTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime() * 1000;
    }
}

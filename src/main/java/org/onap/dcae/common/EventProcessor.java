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
import org.onap.dcae.controller.PersistentEventConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

                /* As long as the producer is running we remove elements from
                 * the queue */
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

            }
        } catch (Exception e) {
            log.error("EventProcessor InterruptedException " + e);
            Thread.currentThread().interrupt();
        }
    }

    private void sendEventsToStreams(String[] streamIdList, EventData ev) {
        for (String aStreamIdList : streamIdList) {
            log.info("Invoking publisher for streamId: " + aStreamIdList);
            if (!ev.getConn().getEventRuleId().equals("")) {
                JSONObject modifiedObj = ev.getEventObj();
                if (ev.getConn().isModifyEvent()){
                    try {
                        log.info("Invoking method " + ev.getConn().getModifyMethod() + " isModify " + ev.getConn().isModifyEvent());
                        modifiedObj = (JSONObject)(this.getClass().getMethod(ev.getConn().getModifyMethod(),
                                EventData.class, String.class).invoke(this, ev, ev.getConn().getUserData()));
                    } catch (Exception e) {
                        log.warn("No such method exist" + e);
                    }
                }
                JSONObject addRuleId = new JSONObject();
                addRuleId.put("rule-id", ev.getConn().getEventRuleId());
                JSONObject customHeader = overrideEvent(addRuleId, modifiedObj);
                customHeader.put("notification-id", ev.getEventObj().getJSONObject("notification").get("notification-id"));
                JSONObject finalObject = overrideEvent(customHeader, addRuleId);
                log.info("Event published" + finalObject);
                eventPublisher.sendEvent(finalObject, aStreamIdList);
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
        
        return mergedJSON;
    }

    public JSONObject modifyOntEvent(EventData ev, String userData) {
        PersistentEventConnection conn = ev.getConn();
        JSONObject json1 = ev.getEventObj();
        log.info("modifyOntEvent");
        JSONObject newJSON = new JSONObject();
        JSONObject finalObj = new JSONObject();
        Path configFile =  Paths.get(conn.getParentCtrllr().getProperties().controllerConfigFileLocation());
        try {
            String bytes = new String(Files.readAllBytes(configFile));
            newJSON = new JSONObject(bytes);
            newJSON.put("serialNumber", json1.getJSONObject("notification").getJSONObject("message").getJSONObject("content").getJSONObject("onu").get("sn"));
            newJSON.put("softwareVersion", json1.getJSONObject("notification").getJSONObject("message").get("version"));

            String refParentLTPNativeId = json1.getJSONObject("notification").getJSONObject("message").getJSONObject("content").getJSONObject("onu").get("refParentLTPNativeId").toString();
            String olt_slot = "";
            String olt_port = "";
            String[] list = refParentLTPNativeId.split(",");
            for (String aList : list) {
                String domain = aList.split("=")[0];
                String value = aList.substring(aList.indexOf('=') + 1);
                switch (domain)
                {
                    case "S":
                        olt_slot = value;
                        break;
                    case "PP":
                        olt_port = value.replaceAll("[^a-zA-Z0-9]", "");
                        break;
                    default:
                        log.info("Field" + domain + " value " + value);
                        break;
                }
            }
            String oltName = json1.getJSONObject("notification").getJSONObject("message").getJSONObject("content").getJSONObject("onu").get("refParentNeNativeId").toString();
            oltName = oltName.substring(3);
            JSONObject additionalfields = newJSON.getJSONObject("additionalFields");
            String attachment_point = oltName + "-" + olt_slot + "-" + olt_port;
            additionalfields.put("attachment-point", attachment_point);
            if (!userData.isEmpty()) {
                java.util.Map<String, String> usrDataMap = parseuserDataToDataHashMapping(userData);
                if (usrDataMap.containsKey("remote_id")) {
                    additionalfields.put("remote-id", usrDataMap.get("remote_id"));
                }

                if (usrDataMap.containsKey("cvlan")) {
                    additionalfields.put("cvlan", usrDataMap.get("cvlan"));
                }

                if (usrDataMap.containsKey("svlan")) {
                    additionalfields.put("svlan", usrDataMap.get("svlan"));
                }

                if (usrDataMap.containsKey("macAddress")) {
                    newJSON.put("macAddress", usrDataMap.get("macAddress"));
                }

                if (usrDataMap.containsKey("vendorName")) {
                    newJSON.put("vendorName", usrDataMap.get("vendorName"));
                }
            }
        } catch (Exception e) {
            log.info("File reading error " + e);
        }
        finalObj.put("pnfRegistration", newJSON);
        log.info("final obj"+ finalObj.toString());
        return finalObj;
    }

    public static java.util.Map<String, String> parseuserDataToDataHashMapping(String userData) {
        java.util.Map<String, String> userDataHash = new HashMap<>();
        String[] list = userData.split("\\;");
        for (String aList : list) {
            String key = aList.split("=")[0];
            String value = aList.substring(aList.indexOf('=') + 1);
            userDataHash.put(key, value);
            log.info("adding key " + key + " value " + value);
        }
        return userDataHash;
    }
}

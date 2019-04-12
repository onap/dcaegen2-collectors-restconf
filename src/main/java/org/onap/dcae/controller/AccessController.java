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
package org.onap.dcae.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.dcae.ApplicationException;
import org.onap.dcae.ApplicationSettings;
import org.onap.dcae.common.Constants;
import org.onap.dcae.common.ControllerActivationState;
import org.onap.dcae.common.RestConfContext;
import org.onap.dcae.common.RestapiCallNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.Files.readAllBytes;
import static org.onap.dcae.common.RestapiCallNodeUtil.getUriMethod;

public class AccessController {
    private static final Logger log = LoggerFactory.getLogger(AccessController.class);
    /* Collector properties */
    private ApplicationSettings properties;

    /* Controller specific information */
    private ControllerConfigInfo cfgInfo;
    private RestConfContext ctx;
    RestapiCallNode restApiCallNode;

    /* Maps of Events */
    private Map<String, PersistentEventConnection> eventList = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Map<String, String> paraMap;

    ControllerActivationState state;

    public AccessController(JSONObject controller,
            ApplicationSettings properties) {
        this.cfgInfo = new ControllerConfigInfo.ControllerConfigInfoBuilder()
                .setController_name(controller.get("controller_name").toString())
                .setController_restapiUrl(controller.get("controller_restapiUrl").toString())
                .setController_restapiUser(controller.get("controller_restapiUser").toString())
                .setController_restapiPassword(controller.get("controller_restapiPassword").toString())
                .setController_accessTokenUrl(controller.get("controller_accessTokenUrl").toString())
                .setController_accessTokenFile(controller.get("controller_accessTokenFile").toString())
                .setController_subscriptionUrl(controller.get("controller_subscriptionUrl").toString())
                .setController_accessTokenMethod(controller.get("controller_accessTokenMethod").toString())
                .setController_subsMethod(controller.get("controller_subsMethod").toString())
                .createControllerConfigInfo();
        this.properties = properties;
        this.ctx = new RestConfContext();
        this.restApiCallNode = new RestapiCallNode();
        this.paraMap = new HashMap<>();
        this.state = ControllerActivationState.INIT;
        prepareControllerParamMap();

        log.info("AccesController Created {} {} {} {} {} {}", this.cfgInfo.getController_name(),
                this.cfgInfo.getController_restapiUrl(), this.cfgInfo.getController_restapiPassword(),
                this.cfgInfo.getController_restapiUser(), this.cfgInfo.getController_accessTokenUrl(),
                this.cfgInfo.getController_accessTokenFile());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AccessController))
            return false;
        AccessController that = (AccessController) o;
        return that.cfgInfo.getController_name().equals(that.cfgInfo.getController_name());
    }

    public ControllerActivationState getState() {
        return state;
    }

    public void setState(ControllerActivationState state) {
        this.state = state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.cfgInfo.getController_name());
    }

    public RestapiCallNode getRestApiCallNode() {
        return restApiCallNode;
    }
    public void setRestApiCallNode(RestapiCallNode node) {
        this.restApiCallNode = node;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    private void fetchTokenId() {

        modifyControllerParamMap(Constants.KSETTING_REST_API_URL, getUriMethod(this.properties.authorizationEnabled()) + cfgInfo.getController_restapiUrl() + cfgInfo.getController_accessTokenUrl());
        modifyControllerParamMap(Constants.KDEFAULT_TEMP_FILENAME, cfgInfo.getController_accessTokenFile());
        modifyControllerParamMap(Constants.KSETTING_REST_UNAME, cfgInfo.getController_restapiUser());
        modifyControllerParamMap(Constants.KSETTING_REST_PASSWD, cfgInfo.getController_restapiPassword());
        modifyControllerParamMap(Constants.KSETTING_HTTP_METHOD, cfgInfo.getController_accessTokenMethod());

        String httpResponse = null;

        try {

            getRestApiCallNode().sendRequest(this.paraMap, ctx, null);
            String key = getControllerParamMapValue(Constants.KSETTING_RESP_PREFIX).concat(".").concat("httpResponse");
            httpResponse = ctx.getAttribute(key);
            log.info("httpResponse ", httpResponse + " key " + key);
            JSONObject jsonObj = new JSONObject(httpResponse);
            log.info("jsonObj ", jsonObj.toString());
            //@TODO: Make return field dynamic
            String tokenId = jsonObj.get("accessSession").toString();
            modifyControllerParamMap(Constants.KSETTING_TOKENID, tokenId);
            modifyControllerParamMap(Constants.KSETTING_CUSTOMHTTP_HEADER, "X-ACCESS-TOKEN=" + tokenId);
            setState(ControllerActivationState.ACTIVE);

        } catch (Exception e) {
            log.info("Access token is not supported " + e.getMessage());
            log.info("http response " + httpResponse);
        }
    }


    public void activate() {
        fetchTokenId();

        if (getState() == ControllerActivationState.ACTIVE) {
            printControllerParamMap();
            /* Create eventlist from properties */
            JSONArray contollers = new JSONArray(properties.rccPolicy());
            for (int i = 0; i < contollers.length(); i++) {
                JSONObject controller = contollers.getJSONObject(i);
                if (controller.get("controller_name").equals(this.getCfgInfo().getController_name())) {
                    JSONArray eventlists = controller.getJSONArray("event_details");
                    for (int j = 0; j < eventlists.length(); j++) {
                        JSONObject event = eventlists.getJSONObject(j);
                        String name = event.get("event_name").toString();
                        PersistentEventConnection conn =
                                new PersistentEventConnection.PersistentEventConnectionBuilder().setEventName(name)
                                        .setEventDescription(event.get("event_description").toString())
                                        .setEventSseventUrlEmbed(
                                                Boolean.parseBoolean(event.get("event_sseventUrlEmbed").toString()))
                                        .setEventSseventsField(event.get("event_sseventsField").toString())
                                        .setEventSseventsUrl(event.get("event_sseventsUrl").toString())
                                        .setEventSubscriptionTemplate(
                                                event.get("event_subscriptionTemplate").toString())
                                        .setEventUnSubscriptionTemplate(
                                                event.get("event_unSubscriptionTemplate").toString())
                                        .setEventRuleId(event.get("event_ruleId").toString()).setParentCtrllr(this)
                                        .setModifyEvent(Boolean.parseBoolean(event.get("modifyData").toString()))
                                        .setModifyMethod(event.get("modifyMethod").toString())
                                        .createPersistentEventConnection();

                        eventList.put(name, conn);
                        executor.execute(conn);
                    }
                }
            }
        }
    }

    public RestConfContext getCtx() {
        return ctx;
    }

    public ApplicationSettings getProperties() {
        return properties;
    }

    public ControllerConfigInfo getCfgInfo() {
        return cfgInfo;
    }

    public Map<String, String> getParaMap() {
        return this.paraMap;
    }

    private void prepareControllerParamMap() {
        /* Adding the fields in ParaMap */
        paraMap.put(Constants.KDEFAULT_TEMP_FILENAME, null);
        paraMap.put(Constants.KSETTING_REST_API_URL, null);
        paraMap.put(Constants.KSETTING_HTTP_METHOD, "post");
        paraMap.put(Constants.KSETTING_RESP_PREFIX, "responsePrefix");
        paraMap.put(Constants.KSETTING_SKIP_SENDING, "false");
        paraMap.put(Constants.KSETTING_SSE_CONNECT_URL, null);
        paraMap.put(Constants.KSETTING_FORMAT, "json");

        paraMap.put(Constants.KSETTING_REST_UNAME, null);
        paraMap.put(Constants.KSETTING_REST_PASSWD, null);
        paraMap.put(Constants.KDEFAULT_REQUESTBODY, null);

        paraMap.put(Constants.KSETTING_AUTH_TYPE, "unspecified");
        paraMap.put(Constants.KSETTING_CONTENT_TYPE, "application/json");
        paraMap.put(Constants.KSETTING_OAUTH_CONSUMER_KEY, null);
        paraMap.put(Constants.KSETTING_OAUTH_CONSUMER_SECRET, null);
        paraMap.put(Constants.KSETTING_OAUTH_SIGNATURE_METHOD, null);
        paraMap.put(Constants.KSETTING_OAUTH_VERSION, null);

        paraMap.put(Constants.KSETTING_CUSTOMHTTP_HEADER, null);
        paraMap.put(Constants.KSETTING_TOKENID, null);
        paraMap.put(Constants.KSETTING_DUMP_HEADER, "false");
        paraMap.put(Constants.KSETTING_RETURN_REQUEST_PAYLOAD, "false");

        paraMap.put(Constants.KSETTING_TRUST_STORE_FILENAME, this.getProperties().truststoreFileLocation());
        String trustPassword = getKeyStorePassword(toAbsolutePath(this.getProperties().truststorePasswordFileLocation()));
        paraMap.put(Constants.KSETTING_TRUST_STORE_PASSWORD, trustPassword);
        paraMap.put(Constants.KSETTING_KEY_STORE_FILENAME, this.getProperties().keystoreFileLocation());
        String KeyPassword = getKeyStorePassword(toAbsolutePath(this.getProperties().keystorePasswordFileLocation()));
        paraMap.put(Constants.KSETTING_KEY_STORE_PASSWD, KeyPassword);

    }

    private Path toAbsolutePath(final String path) {
        return Paths.get(path).toAbsolutePath();
    }

    private String getKeyStorePassword(final Path location) {
        try {
            return new String(readAllBytes(location));
        } catch (Exception e) {
            log.error("Could not read password from: '" + location + "'.", e);
            throw new ApplicationException(e);
        }
    }

    public void modifyControllerParamMap(String fieldName, String value) {
        paraMap.put(fieldName, value);
    }

    public String getControllerParamMapValue(String fieldName) {
        return paraMap.get(fieldName);
    }

    public void printControllerParamMap() {
        log.info("----------------Controller Param Map-------------------");
        for (String name : paraMap.keySet()) {
            String value = paraMap.get(name);
            log.info(name + " : " + value);
        }
    }
}

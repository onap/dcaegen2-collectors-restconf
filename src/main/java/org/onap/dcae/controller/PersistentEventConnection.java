/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2019 Huawei. All rights reserved.
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

import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.onap.dcae.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.onap.dcae.common.RestapiCallNodeUtil.*;

public class PersistentEventConnection implements Runnable {
    public String event_name;
    private String event_description;
    private boolean event_sseventUrlEmbed;
    private String event_sseventsField;
    private String event_sseventsUrl;
    private String event_subscriptionTemplate;
    private String event_unSubscriptionTemplate;
    private String event_ruleId;
    private EventConnectionState state;
    private volatile boolean running = true;
    private static final Logger log = LoggerFactory.getLogger(PersistentEventConnection.class);


    private RestConfContext ctx;
    private AccessController parentCtrllr;
    private Map<String, String> eventParaMap;

    public PersistentEventConnection(String event_name,
                                     String event_description,
                                     boolean event_sseventUrlEmbed,
                                     String event_sseventsField,
                                     String event_sseventsUrl,
                                     String event_subscriptionTemplate,
                                     String event_unSubscriptionTemplate,
                                     String event_ruleId,
                                     AccessController parentCtrllr) {
        this.event_name = event_name;
        this.event_description = event_description;
        this.event_sseventUrlEmbed = event_sseventUrlEmbed;
        this.event_sseventsField = event_sseventsField;
        this.event_sseventsUrl = event_sseventsUrl;
        this.event_subscriptionTemplate = event_subscriptionTemplate;
        this.event_unSubscriptionTemplate = event_unSubscriptionTemplate;
        this.event_ruleId = event_ruleId;
        this.state = EventConnectionState.INIT;

        this.ctx = new RestConfContext();
        for (String s : parentCtrllr.getCtx().getAttributeKeySet()) {
            this.ctx.setAttribute(s, ctx.getAttribute(s));
        }
        this.parentCtrllr = parentCtrllr;
        this.eventParaMap = new HashMap<>();
        this.eventParaMap.putAll(parentCtrllr.getParaMap());
        printEventParamMap();
        log.info("New persistent connection created " + event_name);
    }

    @Override
    public void run() {
        Parameters p = null;
        try {
            modifyEventParamMap(Constants.KSETTING_REST_API_URL, getUriMethod(parentCtrllr.getProperties().authorizationEnabled())
                    + parentCtrllr.getCfgInfo().getController_restapiUrl()
                    + parentCtrllr.getCfgInfo().getController_subscriptionUrl());
            modifyEventParamMap(Constants.KDEFAULT_TEMP_FILENAME, event_subscriptionTemplate);
            modifyEventParamMap(Constants.KSETTING_REST_UNAME, parentCtrllr.getCfgInfo().getController_restapiUser());
            modifyEventParamMap(Constants.KSETTING_REST_PASSWORD, parentCtrllr.getCfgInfo().getController_restapiPassword());
            modifyEventParamMap(Constants.KSETTING_HTTP_METHOD, parentCtrllr.getCfgInfo().getController_subsMethod());

            parentCtrllr.getRestApiCallNode().sendRequest(eventParaMap, ctx, null);
        } catch (Exception e) {
            log.error("Exception occured!", e);
            Thread.currentThread().interrupt();
        }

        /* Retrieve url from result and construct SSE url */
        if (event_sseventUrlEmbed) {
            String key = getEventParamMapValue(Constants.KSETTING_RESP_PREFIX).concat(".").concat(event_sseventsField);
            log.info("key " + key);
            this.event_sseventsUrl = ctx.getAttribute(key);
        }

        log.info("SSE received url " + event_sseventsUrl);
        try {
            p = getParameters(eventParaMap);
        } catch (Exception e) {
            log.error("Exception occured!", e);
            Thread.currentThread().interrupt();
        }
        printEventParamMap();
        String url = getUriMethod(parentCtrllr.getProperties().authorizationEnabled()) +
                parentCtrllr.getCfgInfo().getController_restapiUrl() + event_sseventsUrl;
        Client client = ignoreSslClient().register(SseFeature.class);
        WebTarget target = addAuthType(client, p).target(url);
        String tokenId = getEventParamMapValue(Constants.KSETTING_TOKENID);
        String headerName = "X-ACCESS-TOKEN";
        if (tokenId == null) {
            headerName = HttpHeaders.AUTHORIZATION;
            tokenId = getAuthorizationToken(parentCtrllr.getCfgInfo().getController_restapiUser(),
                    parentCtrllr.getCfgInfo().getController_restapiPassword());
        }
        AdditionalHeaderWebTarget newTarget = new AdditionalHeaderWebTarget(target, tokenId, headerName);
        EventSource eventSource = EventSource.target(newTarget).build();
        eventSource.register(new DataChangeEventListener(this));
        eventSource.open();
        log.info("Connected to SSE source");
        while (running) {
            try {
                log.info("SSE state " + eventSource.isOpen());
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                log.info("Exception: " + ie.getMessage());
                Thread.currentThread().interrupt();
                running = false;
            }
        }
        eventSource.close();
        log.info("Closed connection to SSE source");
    }

    private String getAuthorizationToken(String userName, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((
                userName + ":" + password).getBytes());
    }

    private Client ignoreSslClient() {
        SSLContext sslcontext = null;

        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }

        return ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier((s1, s2) -> true).build();
    }

    public String getEvent_ruleId() {
        return event_ruleId;
    }

    public void modifyEventParamMap(String fieldName, String value) {
        eventParaMap.put(fieldName, value);
    }

    public String getEventParamMapValue(String fieldName) {
        return eventParaMap.get(fieldName);
    }

    public void printEventParamMap() {
        log.info("----------------Event Param Map-------------------");
        for (String name : eventParaMap.keySet()) {
            String value = eventParaMap.get(name);
            log.info(name + " : " + value);
        }
    }
}

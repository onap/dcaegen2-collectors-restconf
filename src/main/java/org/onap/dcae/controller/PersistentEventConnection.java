/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2018-2021 Huawei. All rights reserved.
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.dcae.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.onap.dcae.common.RestapiCallNodeUtil.*;

public class PersistentEventConnection implements Runnable {
    private String event_name;
    private String event_description;
    private boolean event_sseventUrlEmbed;
    private String event_sseventsField;
    private String event_sseventsUrl;
    private String event_subscriptionTemplate;
    private String event_unSubscriptionTemplate;
    private String event_ruleId;
    private EventConnectionState state;
    private volatile boolean running = true;
    private static boolean subscribe = true;
    private static final Logger log = LoggerFactory.getLogger(PersistentEventConnection.class);
    private boolean modifyEvent;
    private String modifyMethod;
    private String userData;
    private RestConfContext ctx;
    private AccessController parentCtrllr;
    private Map<String, String> eventParaMap;


    public static class PersistentEventConnectionBuilder {
        private String event_name;
        private String event_description;
        private boolean event_sseventUrlEmbed;
        private String event_sseventsField;
        private String event_sseventsUrl;
        private String event_subscriptionTemplate;
        private String event_unSubscriptionTemplate;
        private String event_ruleId;
        private AccessController parentCtrllr;
        private boolean modifyEvent;
        private String modifyMethod;
        private String userData;

        public PersistentEventConnectionBuilder setEventName(String event_name) {
            this.event_name = event_name;
            return this;
        }

        public PersistentEventConnectionBuilder setEventDescription(String event_description) {
            this.event_description = event_description;
            return this;
        }

        public PersistentEventConnectionBuilder setEventSseventUrlEmbed(boolean event_sseventUrlEmbed) {
            this.event_sseventUrlEmbed = event_sseventUrlEmbed;
            return this;
        }

        public PersistentEventConnectionBuilder setEventSseventsField(String event_sseventsField) {
            this.event_sseventsField = event_sseventsField;
            return this;
        }

        public PersistentEventConnectionBuilder setEventSseventsUrl(String event_sseventsUrl) {
            this.event_sseventsUrl = event_sseventsUrl;
            return this;
        }

        public PersistentEventConnectionBuilder setEventSubscriptionTemplate(String event_subscriptionTemplate) {
            this.event_subscriptionTemplate = event_subscriptionTemplate;
            return this;
        }

        public PersistentEventConnectionBuilder setEventUnSubscriptionTemplate(String event_unSubscriptionTemplate) {
            this.event_unSubscriptionTemplate = event_unSubscriptionTemplate;
            return this;
        }

        public PersistentEventConnectionBuilder setEventRuleId(String event_ruleId) {
            this.event_ruleId = event_ruleId;
            return this;
        }

        public PersistentEventConnectionBuilder setParentCtrllr(AccessController parentCtrllr) {
            this.parentCtrllr = parentCtrllr;
            return this;
        }

        public PersistentEventConnectionBuilder setModifyEvent(boolean modifyEvent) {
            this.modifyEvent = modifyEvent;
            return this;
        }

        public PersistentEventConnectionBuilder setModifyMethod(String modifyMethod) {
            this.modifyMethod = modifyMethod;
            return this;
        }

        public PersistentEventConnectionBuilder setUserData(String userData) {
            this.userData = userData;
            return this;
        }

        public PersistentEventConnection createPersistentEventConnection() {
            return new PersistentEventConnection(this);
        }


    }


    private PersistentEventConnection(PersistentEventConnectionBuilder builder){

            this.event_name = builder.event_name;
            this.event_description = builder.event_description;
            this.event_sseventUrlEmbed = builder.event_sseventUrlEmbed;
            this.event_sseventsField = builder.event_sseventsField;
            this.event_sseventsUrl = builder.event_sseventsUrl;
            this.event_subscriptionTemplate = builder.event_subscriptionTemplate;
            this.event_unSubscriptionTemplate = builder.event_unSubscriptionTemplate;
            this.event_ruleId = builder.event_ruleId;
            this.state = EventConnectionState.INIT;
            this.modifyEvent = builder.modifyEvent;
            this.modifyMethod = builder.modifyMethod;
            this.userData = builder.userData;

            this.ctx = new RestConfContext();
            for (String s : builder.parentCtrllr.getCtx().getAttributeKeySet()) {
                this.ctx.setAttribute(s, ctx.getAttribute(s));
            }
            this.parentCtrllr = builder.parentCtrllr;
            this.eventParaMap = new HashMap<>();
            this.eventParaMap.putAll(builder.parentCtrllr.getParaMap());
            printEventParamMap();
            log.info("New persistent connection created " + event_name + " modify event " + modifyEvent);

    }

    @Override
    public void run() {
        long sleep_time = 5000;
        boolean openState = false;
        EventSource eventSrc = null;
        while (running) {
            try {
                Thread.sleep(sleep_time);
                if (subscribe) {
                    subscribe = false;
                    subscribe();
                    eventSrc = OpenSseConnection();
                    openState = eventSrc.isOpen();
                    log.info("SSE state " + eventSrc.isOpen());
                }
                if (eventSrc != null && !openState)
                {
                    log.info("SSE state " + eventSrc.isOpen() + " Resubscribing after 1 minute...");
                    Thread.sleep(sleep_time * 12);
                    /* Resubscribe again */
                    subscribe = true;
                }
            } catch (InterruptedException ie) {
                log.info("Exception: " + ie.getMessage());
                running = false;
                if (eventSrc != null) {
                    eventSrc.close();
                }
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e){
                /* Other exception we can keep on retrying */
                log.info("Connection failed: " + e.getMessage());
                subscribe = true;
                if (eventSrc != null) {
                    eventSrc.close();
                    eventSrc = null;
                }
            }
        }
        try {
            if ((eventSrc != null) && (eventSrc.isOpen())) {
                eventSrc.close();
            }
        }catch (Exception e) {
            log.info("Connection close failed: " + e.getMessage());
        }

        log.info("Closed connection to SSE source");
    }

    private void subscribe() {
        try {
            modifyEventParamMap(Constants.KSETTING_REST_API_URL, getUriMethod(parentCtrllr.getProperties().authorizationEnabled())
                    + parentCtrllr.getCfgInfo().getController_restapiUrl()
                    + parentCtrllr.getCfgInfo().getController_subscriptionUrl());
            modifyEventParamMap(Constants.KDEFAULT_TEMP_FILENAME, event_subscriptionTemplate);
            modifyEventParamMap(Constants.KSETTING_REST_UNAME, parentCtrllr.getCfgInfo().getController_restapiUser());
            modifyEventParamMap(Constants.KSETTING_REST_PASSWD, parentCtrllr.getCfgInfo().getController_restapiPassword());
            modifyEventParamMap(Constants.KSETTING_HTTP_METHOD, parentCtrllr.getCfgInfo().getController_subsMethod());
            modifyEventParamMap(Constants.KDEFAULT_DISABLE_SSL, parentCtrllr.getCfgInfo().getController_disableSsl());

            parentCtrllr.getRestApiCallNode().sendRequest(eventParaMap, ctx, null);
        } catch (Exception e) {
            log.error("Failed to receive sbscription notiication, trying again", e);
            try {
                parentCtrllr.getRestApiCallNode().sendRequest(eventParaMap, ctx, null);
            } catch (Exception ex){
                log.error("Exception occured again! Trying again", e);
                Thread.currentThread().interrupt();
            }
        }

        /* Retrieve url from result and construct SSE url */
        if (event_sseventUrlEmbed) {
            String key = getEventParamMapValue(Constants.KSETTING_RESP_PREFIX).concat(".").concat(event_sseventsField);
            log.info("key " + key);
            this.event_sseventsUrl = ctx.getAttribute(key);
        }

        log.info("SSE received url " + event_sseventsUrl);
    }
    private EventSource OpenSseConnection() throws Exception {
        Parameters p = null;

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
        return eventSource;
    }
    private String getAuthorizationToken(String userName, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((
                userName + ":" + password).getBytes());
    }

    private Client ignoreSslClient() throws Exception {
		/*
		 * SSLContext sslcontext = null;
		 * 
		 * try { sslcontext = SSLContext.getInstance("TLS"); sslcontext.init(null, new
		 * TrustManager[]{new X509TrustManager() {
		 * 
		 * @Override public void checkClientTrusted(X509Certificate[] arg0, String arg1)
		 * throws CertificateException { }
		 * 
		 * @Override public void checkServerTrusted(X509Certificate[] arg0, String arg1)
		 * throws CertificateException { }
		 * 
		 * @Override public X509Certificate[] getAcceptedIssuers() { return new
		 * X509Certificate[0]; } }}, new java.security.SecureRandom()); } catch
		 * (NoSuchAlgorithmException | KeyManagementException e) { throw new
		 * IllegalStateException(e); }
		 * 
		 * return
		 * ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier((s1, s2)
		 * -> true).build();
		 */
    	TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    	
    	//Using null here initialises the tmf with default trust store
    	tmf.init((KeyStore)null);
    	
    	//Get hold of default trust manager
    	X509TrustManager x509Tm = null;
    	for(TrustManager tm: tmf.getTrustManagers())
    	{
    		if(tm instanceof X509TrustManager) {
    			x509Tm = (X509TrustManager) tm;
    			break;
    		}
    	}
    	
    	//Wrap it in your own class
    	final X509TrustManager finalTm = x509Tm;
    	X509TrustManager customTm = new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
	         return finalTm.getAcceptedIssuers();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        finalTm.checkServerTrusted(chain, authType);

         }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        finalTm.checkClientTrusted(chain, authType);

         }};

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null,new TrustManager[]{customTm},new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier hostnameverifier = new HostnameVerifier() {
        	@Override
            public boolean verify(String hostname, SSLSession session) {
        		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        		return hv.verify(hostname, session);
        	
        	}};
        	
    	return ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier(hostnameverifier).build();
    }

    public String getEventRuleId() {
        return event_ruleId;
    }

    public void modifyEventParamMap(String fieldName, String value) {
        eventParaMap.put(fieldName, value);
    }

    public String getEventParamMapValue(String fieldName) {
        return eventParaMap.get(fieldName);
    }

    public AccessController getParentCtrllr() {
        return parentCtrllr;
    }

    public boolean isModifyEvent() {
        return modifyEvent;
    }

    public String getModifyMethod() {
        return modifyMethod;
    }

    public String getUserData() {
        return userData;
    }

    public void printEventParamMap() {
        log.info("----------------Event Param Map-------------------");
        for (String name : eventParaMap.keySet()) {
            String value = eventParaMap.get(name);
            log.info(name + " : " + value);
        }
    }

    public Map<String, String> getEventParaMap() {
        return eventParaMap;
    }

    public RestConfContext getCtx() {
        return ctx;
    }

    public void shutdown() {
        running = false;
    }
}

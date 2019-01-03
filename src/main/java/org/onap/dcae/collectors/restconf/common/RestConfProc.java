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

import com.att.nsa.drumlin.till.nv.rrNvReadable;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.dcae.collectors.restconf.common.event.publishing.DMaaPConfigurationParser;
import org.onap.dcae.collectors.restconf.common.event.publishing.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Base64;

import static org.onap.dcae.collectors.restconf.common.RestapiCallNodeUtil.getParameters;
import static org.onap.dcae.collectors.restconf.common.RestapiCallNodeUtil.addAuthType;

public class RestConfProc {

    private static final Logger log = LoggerFactory.getLogger(RestConfProc.class);

    public static String format;

    private static RestConfContext ctx = new RestConfContext();

    private static final Logger oplog = LoggerFactory.getLogger("org.onap.restconf.common.output");

    private Map<String, PersistentConnection> runnableInfo = new ConcurrentHashMap<>();

    private final Map<String, String> paraMap = new HashMap<>();
    private static String cambriaConfigFile;

    public static LinkedBlockingQueue<JSONObject> fProcessingInputQueue;

    public static String streamID;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public RestConfProc() {
    }

    private void parseInputParameters(rrNvReadable settings) {
        String tempFileName;
        String restApiUrl;
        String httpMetthod;
        String respPrefix;
        String skipSending;
        String sseConnectUrl;
        String restapiUser;
        String restapiPassword;
        String trustStoreFileName;
        String trustStorePassword;
        String keyStoreFileName;
        String keyStorePassword;
        String[] currentConfigFile;

        currentConfigFile = settings.getStrings(Constants.KSETTING_DMAAPCONFIGS, Constants.KDEFAULT_DMAAPCONFIGS);
        cambriaConfigFile = currentConfigFile[0];

        tempFileName = settings.getString(Constants.KDEFAULT_TEMP_FILENAME, null);
        restApiUrl = settings.getString(Constants.KSETTING_REST_API_URL, null);
        httpMetthod = settings.getString(Constants.KSETTING_HTTP_METHOD, null);
        respPrefix = settings.getString(Constants.KSETTING_RESP_PREFIX, null);
        skipSending = settings.getString(Constants.KSETTING_SKIP_SENDING, null);
        sseConnectUrl = settings.getString(Constants.KSETTING_SSE_CONNECT_URL, null);
        restapiUser = settings.getString(Constants.KSETTING_UNAME, null);
        restapiPassword = settings.getString(Constants.KSETTING_PASSWORD, null);
        trustStoreFileName = settings.getString(Constants.KSETTING_TRUST_STORE_FILENAME, null);
        trustStorePassword = settings.getString(Constants.KSETTING_TRUST_STORE_PASSWORD, null);
        keyStoreFileName = settings.getString(Constants.KSETTING_KEY_STORE_FILENAME, null);
        keyStorePassword = settings.getString(Constants.KSETTING_KEY_STORE_PASSWORD, null);
        format = settings.getString(Constants.KSETTING_FORMAT, null);
        streamID = "route=route_failure";

        paraMap.put(Constants.KDEFAULT_TEMP_FILENAME, tempFileName);
        paraMap.put(Constants.KSETTING_REST_API_URL, restApiUrl);
        paraMap.put(Constants.KSETTING_HTTP_METHOD, httpMetthod);
        paraMap.put(Constants.KSETTING_RESP_PREFIX, respPrefix);
        paraMap.put(Constants.KSETTING_SKIP_SENDING, skipSending);
        paraMap.put(Constants.KSETTING_SSE_CONNECT_URL, sseConnectUrl);
        paraMap.put(Constants.KSETTING_FORMAT, format);
        paraMap.put(Constants.KSETTING_UNAME, restapiUser);
        paraMap.put(Constants.KSETTING_PASSWORD, restapiPassword);
        paraMap.put(Constants.KSETTING_TRUST_STORE_FILENAME, trustStoreFileName);
        paraMap.put(Constants.KSETTING_TRUST_STORE_PASSWORD, trustStorePassword);
        paraMap.put(Constants.KSETTING_KEY_STORE_FILENAME, keyStoreFileName);
        paraMap.put(Constants.KSETTING_KEY_STORE_PASSWORD, keyStorePassword);

        ctx.setAttribute("prop.encoding-json", "encoding-json");
        ctx.setAttribute("restapi-result.response-code", "200");
        ctx.setAttribute("restapi-result.ietf-subscribed-notifications:output.identifier", "100");
    }

    public RestConfProc(rrNvReadable settings) {

        parseInputParameters(settings);

        fProcessingInputQueue = new LinkedBlockingQueue<>(Constants.KDEFAULT_MAXQUEUEDEVENTS);

        EventProcessor ep = new EventProcessor(EventPublisher.createPublisher(oplog,
                                                                              DMaaPConfigurationParser
                                                                                      .parseToDomainMapping(Paths.get(cambriaConfigFile))
                                                                                      .get()));
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 20; ++i) {
            executor.execute(ep);
        }
    }

    /**
     * To establish a subscription with controller by sending HTTP request
     *
     * @param paramMap holds the input configuration
     * @param ctx      restconf context
     * @param url      url to send subscription request
     * @throws Exception exception
     */
    public void establishSubscription(Map<String, String> paramMap,
                                      RestConfContext ctx,
                                      String url) throws Exception {

        RestapiCallNode restApiCallNode = new RestapiCallNode();

        Map<String, String> params = new HashMap<>();
        params.put("restapiUrl", "https://" + url + "/controller/v2/tokens");
        params.put("httpMethod", "post");
        params.put("templateFileName", "./etc/access-token.json");
        params.put("skipSending", "false");
        params.put("format", "json");
        params.put("restapiUser", "test123");
        params.put("restapiPassword", "Changeme_123");
        params.put(Constants.KSETTING_TRUST_STORE_FILENAME,
                   paramMap.get(Constants.KSETTING_TRUST_STORE_FILENAME));
        params.put(Constants.KSETTING_TRUST_STORE_PASSWORD, "adminadmin");
        params.put(Constants.KSETTING_KEY_STORE_FILENAME,
                   paramMap.get(Constants.KSETTING_KEY_STORE_FILENAME));
        params.put(Constants.KSETTING_KEY_STORE_PASSWORD, "adminadmin");

        String httpResponse = null;
        try {
            restApiCallNode.sendRequest(params, ctx, null);
            httpResponse = ctx.getAttribute("httpResponse");
            JSONObject jsonObj = new JSONObject(httpResponse);
            JSONObject data = jsonObj.getJSONObject("data");
            String tokenId = data.get("token_id").toString();
            paramMap.put("customHttpHeaders", "X-ACCESS-TOKEN=" + tokenId);
            paramMap.put("TokenId", tokenId);
        } catch (Exception e) {
            log.info("Access token is not supported" + e.getMessage());
            log.info("http response" + httpResponse);
        }

        restApiCallNode.sendRequest(paramMap, ctx, null);

        establishPersistentConnection(paramMap, ctx);
    }

    /**
     * To establish persistent connection after receiving successful subscription response from controller
     *
     * @param paramMap holds the input configuration
     * @param ctx      restconf context
     */
    public void establishPersistentConnection(Map<String, String> paramMap, RestConfContext ctx) {

        // check whether response is ok
        if (ctx.getAttribute(Constants.RESPONSE_CODE).equals(Constants.RESPONSE_CODE_200)) {

            String id = ctx.getAttribute(Constants.OUTPUT_IDENTIFIER);

            String url = paramMap.get(Constants.KSETTING_SSE_CONNECT_URL);

            PersistentConnection connection = new PersistentConnection(url, ctx, paramMap);
            runnableInfo.put(id, connection);
            executor.execute(connection);
        } else {
            // error response is already updated in ctx
            log.info("Failed to subscribe");
        }
    }

    /**
     * Get input parameter map
     *
     * @return input parameters map
     */
    public Map<String, String> getParaMap() {
        return paraMap;
    }


    /**
     * Get restConf context which has information about message encoding type
     *
     * @return restconf context
     */
    public RestConfContext getCtx() {
        return ctx;
    }

    public class PersistentConnection implements Runnable {
        private String url;
        private RestConfContext ctx;
        private Map<String, String> paramMap;
        private volatile boolean running = true;

        public PersistentConnection(String url, RestConfContext ctx, Map<String, String> paramMap) {
            this.url = url;
            this.ctx = ctx;
            this.paramMap = paramMap;
        }

        @Override
        public void run() {
            Parameters p = null;
            try {
                p = getParameters(paramMap);
            } catch (Exception e) {
                log.error("Exception occured!", e);
                Thread.currentThread().interrupt();
            }

            Client client =  ignoreSslClient().register(SseFeature.class);
            WebTarget target = addAuthType(client, p).target(url);
            String token = paramMap.get("TokenId");
            String headerName = "X-ACCESS-TOKEN";
            if (token == null) {
                headerName = HttpHeaders.AUTHORIZATION;
                if(null!=p) {
                	token = getAuthorizationToken(p.restapiUser, p.restapiPassword);
                }
            }
            AdditionalHeaderWebTarget newTarget = new AdditionalHeaderWebTarget(target, token, headerName);
            EventSource eventSource = EventSource.target(newTarget).build();
            eventSource.register(new DataChangeEventListener(ctx));
            eventSource.open();
            log.debug("Connected to SSE source");
            while (running) {
                try {
                    log.debug("SSE state " + eventSource.isOpen());
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    log.debug("Exception: " + ie.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            eventSource.close();
            log.info("Closed connection to SSE source");
        }
    }

    private String getAuthorizationToken(String userName, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((
                userName + ":" + password).getBytes());
    }

    /**
     * To process the array of events which are received from controller
     *
     * @param a JSONArray
     * @throws Exception exception
     */
    public static void handleEvents(JSONArray a) throws Exception {
        for (int i = 0; i < a.length(); i++) {
            if (!fProcessingInputQueue.offer(a.getJSONObject(i))) {
                throw new Exception();
            }
        }
        log.debug("RestConfCollector.handleEvents:EVENTS has been published successfully!");
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
            } }, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }

        return ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier((s1, s2) -> true).build();
    }
}



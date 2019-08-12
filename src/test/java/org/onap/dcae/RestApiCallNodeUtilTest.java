/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
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

package org.onap.dcae;

import org.junit.Test;
import org.onap.dcae.common.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by koblosz on 07.06.18.
 */
public class RestApiCallNodeUtilTest {


    @Test
    public void testParseParameter() {
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put(Constants.KDEFAULT_TEMP_FILENAME, null);
        paraMap.put(Constants.KSETTING_REST_API_URL, "https://127.0.0.1:8080/rest/sample");
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

        paraMap.put(Constants.KSETTING_TRUST_STORE_FILENAME, null);
        String trustPassword = "admin";
        paraMap.put(Constants.KSETTING_TRUST_STORE_PASSWORD, trustPassword);
        paraMap.put(Constants.KSETTING_KEY_STORE_FILENAME, null);
        String KeyPassword = "admin";
        paraMap.put(Constants.KSETTING_KEY_STORE_PASSWD, KeyPassword);
        try {
            Parameters p = RestapiCallNodeUtil.getParameters(paraMap);
            assertEquals(p.contentType, "application/json");
        } catch (Exception e) {}

    }

    @Test
    public void parseComplexParam() {
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put(Constants.KDEFAULT_TEMP_FILENAME, null);
        paraMap.put(Constants.KSETTING_REST_API_URL, "https://sonar.onap.org/component_measures?id=org.onap.dcaegen2.collectors.restconf%3Arestconfcollector&metric=coverage&selected=org.onap.dcaegen2.collectors.restconf%3Arestconfcollector%3Asrc%2Fmain%2Fjava%2Forg%2Fonap%2Fdcae%2Fcommon%2FRestapiCallNodeUtil.java");
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

        paraMap.put(Constants.KSETTING_TRUST_STORE_FILENAME, null);
        String trustPassword = "admin";
        paraMap.put(Constants.KSETTING_TRUST_STORE_PASSWORD, trustPassword);
        paraMap.put(Constants.KSETTING_KEY_STORE_FILENAME, null);
        String KeyPassword = "admin";
        paraMap.put(Constants.KSETTING_KEY_STORE_PASSWD, KeyPassword);
        try {
            Parameters p = RestapiCallNodeUtil.getParameters(paraMap);
            assertEquals(p.contentType, "application/json");
        } catch (Exception e) {}
    }

    @Test
    public void parseinValidUrl() {
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put(Constants.KSETTING_REST_API_URL, "");
        try {
            Parameters p = RestapiCallNodeUtil.getParameters(paraMap);
        } catch (Exception e) {}
    }

    @Test
    public void addAuthTypeTest() {

        Map<String, String> paraMap = new HashMap<>();
        paraMap.put(Constants.KDEFAULT_TEMP_FILENAME, null);
        paraMap.put(Constants.KSETTING_REST_API_URL, "https://127.0.0.1:8080/rest/sample");
        paraMap.put(Constants.KSETTING_HTTP_METHOD, "post");
        paraMap.put(Constants.KSETTING_RESP_PREFIX, "responsePrefix");
        paraMap.put(Constants.KSETTING_SKIP_SENDING, "false");
        paraMap.put(Constants.KSETTING_SSE_CONNECT_URL, null);
        paraMap.put(Constants.KSETTING_FORMAT, "json");

        paraMap.put(Constants.KSETTING_REST_UNAME, "collector");
        paraMap.put(Constants.KSETTING_REST_PASSWD, "collector");
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

        paraMap.put(Constants.KSETTING_TRUST_STORE_FILENAME, null);
        String trustPassword = "admin";
        paraMap.put(Constants.KSETTING_TRUST_STORE_PASSWORD, trustPassword);
        paraMap.put(Constants.KSETTING_KEY_STORE_FILENAME, null);
        String KeyPassword = "admin";
        paraMap.put(Constants.KSETTING_KEY_STORE_PASSWD, KeyPassword);


        Client client = ClientBuilder.newBuilder().build();
        try {
            Parameters p = RestapiCallNodeUtil.getParameters(paraMap);
            p.restapiUser = "restapiUser";
            p.restapiPassword = "restapiPassword";
            RestapiCallNodeUtil.addAuthType(client, p);

            p.restapiUser = null;
            p.restapiPassword = null;
            p.oAuthConsumerKey = "restapiUser";
            p.oAuthSignatureMethod = "restapiPassword";
            p.oAuthConsumerSecret = "someval";
            RestapiCallNodeUtil.addAuthType(client, p);

            p.authtype = AuthType.DIGEST;
            p.restapiUser = "restapiUser";
            p.restapiPassword = "restapiPassword";
            RestapiCallNodeUtil.addAuthType(client, p);
        } catch (Exception e) {}

        try {
            Parameters p = RestapiCallNodeUtil.getParameters(paraMap);
            p.authtype = AuthType.BASIC;
            p.restapiUser = "restapiUser";
            p.restapiPassword = "restapiPassword";
            RestapiCallNodeUtil.addAuthType(client, p);

        } catch (Exception e) {}

        try {
            Parameters p = RestapiCallNodeUtil.getParameters(paraMap);
            p.authtype = AuthType.OAUTH;
            p.oAuthConsumerKey = "restapiUser";
            p.oAuthSignatureMethod = "restapiPassword";
            p.oAuthConsumerSecret = "someval";
            RestapiCallNodeUtil.addAuthType(client, p);

        } catch (Exception e) {}
    }
}
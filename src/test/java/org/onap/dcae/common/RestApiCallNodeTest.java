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

package org.onap.dcae.common;

import com.sun.jersey.api.client.WebResource;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RestApiCallNodeTest {
    @Test
    public void RestApiCallNodeTestSendEmptyMessageNoTemplate() {
        RestapiCallNode rest = new RestapiCallNode();
        RestConfContext ctx = new RestConfContext();
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


        WebResource webResource = mock(WebResource.class);
        WebResource.Builder webResourceBuilder = mock(WebResource.Builder.class);
        ClientResponse clientResponse = mock(ClientResponse.class);
        try {
            Mockito.doNothing().when(webResourceBuilder).method("post");
            when(webResource.accept(anyString())).thenReturn(webResourceBuilder);

            rest.sendRequest(paraMap, ctx, 1);
        }catch (Exception e){}
    }

    @Test
    public void RestApiCallNodeTestSendEmptyMessageWithTemplate() {
        RestapiCallNode rest = new RestapiCallNode();
        RestConfContext ctx = new RestConfContext();
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put(Constants.KDEFAULT_TEMP_FILENAME, "src/test/resources/testTemplatefile");
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


        WebResource webResource = mock(WebResource.class);
        WebResource.Builder webResourceBuilder = mock(WebResource.Builder.class);
        ClientResponse clientResponse = mock(ClientResponse.class);
        try {
            Mockito.doNothing().when(webResourceBuilder).method("post");
            when(webResource.accept(anyString())).thenReturn(webResourceBuilder);

            rest.sendRequest(paraMap, ctx, 1);
        }catch (Exception e){}
    }

}
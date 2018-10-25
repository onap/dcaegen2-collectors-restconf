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

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth1.ConsumerCredentials;
import org.glassfish.jersey.client.oauth1.OAuth1ClientSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Feature;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RestapiCallNodeUtil {

    private static final Logger log = LoggerFactory.getLogger(RestapiCallNodeUtil.class);

    private RestapiCallNodeUtil() {
        // Preventing instantiation of the same.
    }

    public static Parameters getParameters(Map<String, String> paramMap) throws Exception {
        Parameters p = new Parameters();
        p.templateFileName = parseParam(paramMap, "templateFileName", false, null);
        p.requestBody = parseParam(paramMap, "requestBody", false, null);
        p.restapiUrl = parseParam(paramMap, "restapiUrl", true, null);
        validateUrl(p.restapiUrl);
        p.restapiUser = parseParam(paramMap, "restapiUser", false, null);
        p.restapiPassword = parseParam(paramMap, "restapiPassword", false, null);
        p.oAuthConsumerKey = parseParam(paramMap, "oAuthConsumerKey", false, null);
        p.oAuthConsumerSecret = parseParam(paramMap, "oAuthConsumerSecret", false, null);
        p.oAuthSignatureMethod = parseParam(paramMap, "oAuthSignatureMethod", false, null);
        p.oAuthVersion = parseParam(paramMap, "oAuthVersion", false, null);
        p.contentType = parseParam(paramMap, "contentType", false, null);
        p.format = Format.fromString(parseParam(paramMap, "format", false, "json"));
        p.authtype = AuthType.fromString(parseParam(paramMap, "authType", false, "unspecified"));
        p.httpMethod = HttpMethod.fromString(parseParam(paramMap, "httpMethod", false, "post"));
        p.responsePrefix = parseParam(paramMap, "responsePrefix", false, null);
        p.listNameList = getListNameList(paramMap);
        String skipSendingStr = paramMap.get("skipSending");
        p.skipSending = "true".equalsIgnoreCase(skipSendingStr);
        p.convertResponse = Boolean.valueOf(parseParam(paramMap, "convertResponse", false, "true"));
        p.trustStoreFileName = parseParam(paramMap, "trustStoreFileName", false, null);
        p.trustStorePassword = parseParam(paramMap, "trustStorePassword", false, null);
        p.keyStoreFileName = parseParam(paramMap, "keyStoreFileName", false, null);
        p.keyStorePassword = parseParam(paramMap, "keyStorePassword", false, null);
        p.ssl = p.trustStoreFileName != null && p.trustStorePassword != null && p.keyStoreFileName != null &&
                p.keyStorePassword != null;
        p.customHttpHeaders = parseParam(paramMap, "customHttpHeaders", false, null);
        p.partner = parseParam(paramMap, "partner", false, null);
        p.dumpHeaders = Boolean.valueOf(parseParam(paramMap, "dumpHeaders", false, null));
        p.returnRequestPayload = Boolean.valueOf(parseParam(paramMap, "returnRequestPayload", false, null));
        return p;
    }

    public static String parseParam(Map<String, String> paramMap, String name, boolean required, String def)
            throws Exception {
        String s = paramMap.get(name);

        if (s == null || s.trim().length() == 0) {
            if (!required) {
                return def;
            }
            throw new Exception("Parameter " + name + " is required in RestapiCallNode");
        }

        s = s.trim();
        StringBuilder value = new StringBuilder();
        int i = 0;
        int i1 = s.indexOf('%');
        while (i1 >= 0) {
            int i2 = s.indexOf('%', i1 + 1);
            if (i2 < 0) {
                break;
            }

            String varName = s.substring(i1 + 1, i2);
            String varValue = System.getenv(varName);
            if (varValue == null) {
                varValue = "%" + varName + "%";
            }

            value.append(s.substring(i, i1));
            value.append(varValue);

            i = i2 + 1;
            i1 = s.indexOf('%', i);
        }
        value.append(s.substring(i));

        log.info("Parameter {}: [{}]", name, value);
        return value.toString();
    }

    public static void validateUrl(String restapiUrl) throws Exception {
        try {
            URI.create(restapiUrl);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid input of url " + e.getLocalizedMessage(), e);
        }
    }

    public static Set<String> getListNameList(Map<String, String> paramMap) {
        Set<String> ll = new HashSet<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet())
            if (entry.getKey().startsWith("listName")) {
                ll.add(entry.getValue());
            }
        return ll;
    }

    public static Client addAuthType(Client client, Parameters p) {
        if (p.authtype == AuthType.Unspecified) {
            if (p.restapiUser != null && p.restapiPassword != null) {
                client.register(HttpAuthenticationFeature.basic(p.restapiUser, p.restapiPassword));
            } else if (p.oAuthConsumerKey != null && p.oAuthConsumerSecret != null
                    && p.oAuthSignatureMethod != null) {
                Feature oAuth1Feature = OAuth1ClientSupport
                        .builder(new ConsumerCredentials(p.oAuthConsumerKey, p.oAuthConsumerSecret))
                        .version(p.oAuthVersion).signatureMethod(p.oAuthSignatureMethod).feature().build();
                client.register(oAuth1Feature);
            }
        } else {
            if (p.authtype == AuthType.DIGEST) {
                if (p.restapiUser != null && p.restapiPassword != null) {
                    client.register(HttpAuthenticationFeature.digest(p.restapiUser, p.restapiPassword));
                } else {
                    throw new IllegalArgumentException(
                            "oAUTH authentication type selected but all restapiUser and restapiPassword " +
                                    "parameters doesn't exist", new Throwable());
                }
            } else if (p.authtype == AuthType.BASIC) {
                if (p.restapiUser != null && p.restapiPassword != null) {
                    client.register(HttpAuthenticationFeature.basic(p.restapiUser, p.restapiPassword));
                } else {
                    throw new IllegalArgumentException(
                            "oAUTH authentication type selected but all restapiUser and restapiPassword " +
                                    "parameters doesn't exist", new Throwable());
                }
            } else if (p.authtype == AuthType.OAUTH) {
                if (p.oAuthConsumerKey != null && p.oAuthConsumerSecret != null && p.oAuthSignatureMethod != null) {
                    Feature oAuth1Feature = OAuth1ClientSupport
                            .builder(new ConsumerCredentials(p.oAuthConsumerKey, p.oAuthConsumerSecret))
                            .version(p.oAuthVersion).signatureMethod(p.oAuthSignatureMethod).feature().build();
                    client.register(oAuth1Feature);
                } else {
                    throw new IllegalArgumentException(
                            "oAUTH authentication type selected but all oAuthConsumerKey, oAuthConsumerSecret " +
                                    "and oAuthSignatureMethod parameters doesn't exist", new Throwable());
                }
            }
        }
        return client;
    }
}


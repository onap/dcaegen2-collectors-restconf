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

package org.onap.dcae.common;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.HTTPDigestAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.onap.dcae.common.RestapiCallNodeUtil.getParameters;
import static org.onap.dcae.common.RestapiCallNodeUtil.parseParam;

public class RestapiCallNode {
    private static final Logger log = LoggerFactory.getLogger(RestapiCallNode.class);

    public void sendRequest(Map<String, String> paramMap, RestConfContext ctx, Integer retryCount) throws Exception {
        HttpResponse r = new HttpResponse();
        try {
            Parameters p = getParameters(paramMap);
            String pp = p.responsePrefix != null ? p.responsePrefix + '.' : "";
            String req = null;
            if (p.templateFileName != null) {
                log.info("p.templateFileName " + p.templateFileName);
                String reqTemplate = readFile(p.templateFileName);
                req = buildXmlJsonRequest(ctx, reqTemplate, p.format);
            } else if (p.requestBody != null) {
                req = p.requestBody;
            }

            r = sendHttpRequest(req, p);
            setResponseStatus(ctx, p.responsePrefix, r);

            if (p.dumpHeaders && r.headers != null) {
                for (Map.Entry<String, List<String>> a : r.headers.entrySet()) {
                    ctx.setAttribute(pp + "header." + a.getKey(), StringUtils.join(a.getValue(), ","));
                }
            }

            if (p.returnRequestPayload && req != null) {
                ctx.setAttribute(pp + "httpRequest", req);
            }

            if (r.body != null && r.body.trim().length() > 0) {
                ctx.setAttribute(pp + "httpResponse", r.body);

                if (p.convertResponse) {
                    Map<String, String> mm = null;
                    if (p.format == Format.XML) {
                        mm = XmlParser.convertToProperties(r.body, p.listNameList);
                    } else if (p.format == Format.JSON) {
                        mm = JsonParser.convertToProperties(r.body);
                    }

                    if (mm != null) {
                        for (Map.Entry<String, String> entry : mm.entrySet()) {
                            ctx.setAttribute(pp + entry.getKey(), entry.getValue());
                            log.info("ctx.setAttribute :=>  {} value {}  ", pp + entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            boolean shouldRetry = false;
            if (e.getCause().getCause() instanceof SocketException) {
                shouldRetry = true;
            }

            log.error("Error sending the request: " + e.getMessage(), e);
            String prefix = parseParam(paramMap, "responsePrefix", false, null);
            if (!shouldRetry || (retryCount == null) || (retryCount == 0)) {
                setFailureResponseStatus(ctx, prefix, e.getMessage(), r);
            } else {
                try {
                    retryCount = retryCount - 1;
                    log.debug("This is retry attempt {} ", retryCount);
                    sendRequest(paramMap, ctx, retryCount);
                } catch (Exception ex) {
                    log.error("Could not attempt retry.", ex);
                    String retryErrorMessage =
                            "Retry attempt has failed. No further retry shall be attempted, calling " +
                                    "setFailureResponseStatus.";
                    setFailureResponseStatus(ctx, prefix, retryErrorMessage, r);
                }
            }
        }

        if (r != null && r.code >= 300) {
            throw new Exception(String.valueOf(r.code) + ": " + r.message);
        }
    }

    protected String buildXmlJsonRequest(RestConfContext ctx, String template, Format format) throws Exception {
        log.info("Building {} started", format);
        long t1 = System.currentTimeMillis();

        template = expandRepeats(ctx, template, 1);

        Map<String, String> mm = new HashMap<>();
        for (String s : ctx.getAttributeKeySet()) {
            mm.put(s, ctx.getAttribute(s));
        }
        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int i1 = template.indexOf("${", i);
            if (i1 < 0) {
                ss.append(template.substring(i));
                break;
            }

            int i2 = template.indexOf('}', i1 + 2);
            if (i2 < 0) {
                throw new Exception("Template error: Matching } not found");
            }

            String var1 = template.substring(i1 + 2, i2);
            String value1 = format == Format.XML ? XmlJsonUtil.getXml(mm, var1) : XmlJsonUtil.getJson(mm, var1);
            if (value1 == null || value1.trim().length() == 0) {
                // delete the whole element (line)
                int i3 = template.lastIndexOf('\n', i1);
                if (i3 < 0) {
                    i3 = 0;
                }
                int i4 = template.indexOf('\n', i1);
                if (i4 < 0) {
                    i4 = template.length();
                }

                if (i < i3) {
                    ss.append(template.substring(i, i3));
                }
                i = i4;
            } else {
                ss.append(template.substring(i, i1)).append(value1);
                i = i2 + 1;
            }
        }

        String req = format == Format.XML
                ? XmlJsonUtil.removeEmptyStructXml(ss.toString()) : XmlJsonUtil.removeEmptyStructJson(ss.toString());

        if (format == Format.JSON) {
            req = XmlJsonUtil.removeLastCommaJson(req);
        }

        long t2 = System.currentTimeMillis();
        log.info("Building {} completed. Time: {}", format, (t2 - t1));

        return req;
    }

    protected String expandRepeats(RestConfContext ctx, String template, int level) throws Exception {
        StringBuilder newTemplate = new StringBuilder();
        int k = 0;
        while (k < template.length()) {
            int i1 = template.indexOf("${repeat:", k);
            if (i1 < 0) {
                newTemplate.append(template.substring(k));
                break;
            }

            int i2 = template.indexOf(':', i1 + 9);
            if (i2 < 0) {
                throw new Exception(
                        "Template error: Context variable name followed by : is required after repeat");
            }

            // Find the closing }, store in i3
            int nn = 1;
            int i3 = -1;
            int i = i2;
            while (nn > 0 && i < template.length()) {
                i3 = template.indexOf('}', i);
                if (i3 < 0) {
                    throw new Exception("Template error: Matching } not found");
                }
                int i32 = template.indexOf('{', i);
                if (i32 >= 0 && i32 < i3) {
                    nn++;
                    i = i32 + 1;
                } else {
                    nn--;
                    i = i3 + 1;
                }
            }

            String var1 = template.substring(i1 + 9, i2);
            String value1 = ctx.getAttribute(var1);
            log.info("     {}:{}", var1, value1);
            int n = 0;
            try {
                n = Integer.parseInt(value1);
            } catch (NumberFormatException e) {
                log.info("value1 not set or not a number, n will remain set at zero");
            }

            newTemplate.append(template.substring(k, i1));

            String rpt = template.substring(i2 + 1, i3);

            for (int ii = 0; ii < n; ii++) {
                String ss = rpt.replaceAll("\\[\\$\\{" + level + "\\}\\]", "[" + ii + "]");
                if (ii == n - 1 && ss.trim().endsWith(",")) {
                    int i4 = ss.lastIndexOf(',');
                    if (i4 > 0) {
                        ss = ss.substring(0, i4) + ss.substring(i4 + 1);
                    }
                }
                newTemplate.append(ss);
            }

            k = i3 + 1;
        }

        if (k == 0) {
            return newTemplate.toString();
        }

        return expandRepeats(ctx, newTemplate.toString(), level + 1);
    }

    protected String readFile(String fileName) throws Exception {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, "UTF-8");
        } catch (IOException | SecurityException e) {
            throw new IOException("Unable to read file " + fileName + e.getLocalizedMessage(), e);
        }
    }

    protected Client addAuthType(Client client, Parameters p) throws Exception {
        if (p.authtype == AuthType.Unspecified) {
            if (p.restapiUser != null && p.restapiPassword != null) {
                client.addFilter(new HTTPBasicAuthFilter(p.restapiUser, p.restapiPassword));
            } else if (p.oAuthConsumerKey != null && p.oAuthConsumerSecret != null
                    && p.oAuthSignatureMethod != null) {
                OAuthParameters params = new OAuthParameters()
                        .signatureMethod(p.oAuthSignatureMethod)
                        .consumerKey(p.oAuthConsumerKey)
                        .version(p.oAuthVersion);

                OAuthSecrets secrets = new OAuthSecrets()
                        .consumerSecret(p.oAuthConsumerSecret);
                client.addFilter(new OAuthClientFilter(client.getProviders(), params, secrets));
            }
        } else {
            if (p.authtype == AuthType.DIGEST) {
                if (p.restapiUser != null && p.restapiPassword != null) {
                    client.addFilter(new HTTPDigestAuthFilter(p.restapiUser, p.restapiPassword));
                } else {
                    throw new SecurityException("oAUTH authentication type selected but all restapiUser and restapiPassword " +
                            "parameters doesn't exist", new Throwable());
                }
            } else if (p.authtype == AuthType.BASIC) {
                if (p.restapiUser != null && p.restapiPassword != null) {
                    client.addFilter(new HTTPBasicAuthFilter(p.restapiUser, p.restapiPassword));
                } else {
                    throw new SecurityException("oAUTH authentication type selected but all restapiUser and restapiPassword " +
                            "parameters doesn't exist", new Throwable());
                }
            } else if (p.authtype == AuthType.OAUTH) {
                if (p.oAuthConsumerKey != null && p.oAuthConsumerSecret != null && p.oAuthSignatureMethod != null) {
                    OAuthParameters params = new OAuthParameters()
                            .signatureMethod(p.oAuthSignatureMethod)
                            .consumerKey(p.oAuthConsumerKey)
                            .version(p.oAuthVersion);

                    OAuthSecrets secrets = new OAuthSecrets()
                            .consumerSecret(p.oAuthConsumerSecret);
                    client.addFilter(new OAuthClientFilter(client.getProviders(), params, secrets));
                } else {
                    throw new SecurityException("oAUTH authentication type selected but all oAuthConsumerKey, voAuthConsumerSecret " +
                            "and oAuthSignatureMethod parameters doesn't exist", new Throwable());
                }
            }
        }
        return client;
    }

    protected HttpResponse sendHttpRequest(String request, Parameters p) throws Exception {
        /* Enable this code if external controller's keyStore file not availabale */
        ClientConfig config = new DefaultClientConfig();
        if (!p.disableSsl) {
            SSLContext ssl = null;
            if (p.ssl && p.restapiUrl.startsWith("https")) {
                ssl = createSSLContext(p);
            }
            if (ssl != null) {
             //   HostnameVerifier hostnameVerifier = (hostname, session) -> true;
                HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                	@Override
                	public boolean verify(String hostname,SSLSession session) {
                		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                		return hv.verify(hostname, session);
                	}
                };
                
                config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                        new HTTPSProperties(hostnameVerifier, ssl));
            }
        } else {

        	 /* Create a trust manager that does not validate certificate chains 
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

             Install the all-trusting trust manager 
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

             Create all-trusting host name verifier 
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

             Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);*/
        	
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

            HttpsURLConnection.setDefaultHostnameVerifier(hostnameverifier);
        }
        logProperties(config.getProperties());

        Client client = Client.create(config);
        client.setConnectTimeout(5000);
        client.addFilter(new LoggingFilter());
        WebResource webResource = addAuthType(client, p).resource(p.restapiUrl);

        log.info("Sending request:");
        log.info(request);
        log.info("URL: " + p.restapiUrl + " method " + p.httpMethod.toString() + " Custom header " + p.customHttpHeaders);

        long t1 = System.currentTimeMillis();

        HttpResponse r = new HttpResponse();
        r.code = 200;

        if (!p.skipSending) {
            String tt = p.format == Format.XML ? "application/xml" : "application/json";
            String tt1 = tt + ";charset=UTF-8";
            if (p.contentType != null) {
                tt = p.contentType;
                tt1 = p.contentType;
            }

            WebResource.Builder webResourceBuilder = webResource.accept(tt).type(tt1);
            if (p.format == Format.NONE) {
                webResourceBuilder = webResource.header("", "");
            }

            if (p.customHttpHeaders != null && p.customHttpHeaders.length() > 0) {
                String[] keyValuePairs = p.customHttpHeaders.split(",");
                for (String singlePair : keyValuePairs) {
                    int equalPosition = singlePair.indexOf('=');
                    webResourceBuilder.header(singlePair.substring(0, equalPosition),
                            singlePair.substring(equalPosition + 1, singlePair.length()));
                }
            }

            ClientResponse response;

            try {
                response = webResourceBuilder.method(p.httpMethod.toString(), ClientResponse.class, request);
            } catch (UniformInterfaceException | ClientHandlerException e) {
                throw new Exception("Exception while sending http request to client "
                        + e.getLocalizedMessage(), e);
            }

            r.code = response.getStatus();
            r.headers = response.getHeaders();
            EntityTag etag = response.getEntityTag();
            if (etag != null) {
                r.message = etag.getValue();
            }
            if (response.hasEntity() && r.code != 204) {
                r.body = response.getEntity(String.class);
            }
        }

        long t2 = System.currentTimeMillis();
        log.info("Response received. Time: {}", (t2 - t1));
        log.info("HTTP response code: {}", r.code);
        log.info("HTTP response message: {}", r.message);
        logHeaders(r.headers);
        log.info("HTTP response: {}", r.body);

        return r;
    }

    protected void setFailureResponseStatus(RestConfContext ctx, String prefix, String errorMessage,
                                            HttpResponse resp) {
        resp.code = 500;
        resp.message = errorMessage;
        String pp = prefix != null ? prefix + '.' : "";
        ctx.setAttribute(pp + "response-code", String.valueOf(resp.code));
        ctx.setAttribute(pp + "response-message", resp.message);
    }

    protected void setResponseStatus(RestConfContext ctx, String prefix, HttpResponse r) {
        String pp = prefix != null ? prefix + '.' : "";
        ctx.setAttribute(pp + "response-code", String.valueOf(r.code));
        ctx.setAttribute(pp + "response-message", r.message);
    }

    protected SSLContext createSSLContext(Parameters p) {
        try (FileInputStream in = new FileInputStream(p.keyStoreFileName)) {
            System.setProperty("jsse.enableSNIExtension", "false");
            System.setProperty("javax.net.ssl.trustStore", p.trustStoreFileName);
            System.setProperty("javax.net.ssl.trustStorePassword", p.trustStorePassword);

          //  HttpsURLConnection.setDefaultHostnameVerifier((string, ssls) -> true);
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            	@Override
            	public boolean verify(String hostname,SSLSession session) {
            		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            		return hv.verify(hostname, session);
            	}
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] pwd = p.keyStorePassword.toCharArray();
            log.info("pwd " + pwd + " " + p.keyStorePassword);
            ks.load(in, pwd);
            kmf.init(ks, pwd);

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), null, null);
            return ctx;
        } catch (Exception e) {
            log.error("Error creating SSLContext: {}", e.getMessage(), e);
        }
        return null;
    }

    protected void logProperties(Map<String, Object> mm) {
        List<String> ll = new ArrayList<>();
        for (Object o : mm.keySet())
            ll.add((String) o);
        Collections.sort(ll);

        log.info("Properties:");
        for (String name : ll)
            log.info("--- {}:{}", name, String.valueOf(mm.get(name)));
    }

    protected void logHeaders(MultivaluedMap<String, String> mm) {
        log.info("HTTP response headers:");

        if (mm == null) {
            return;
        }

        List<String> ll = new ArrayList<>();
        for (Object o : mm.keySet())
            ll.add((String) o);
        Collections.sort(ll);

        for (String name : ll)
            log.info("--- {}:{}", name, String.valueOf(mm.get(name)));
    }
}
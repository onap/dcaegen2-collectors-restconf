/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Try;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Java6Assertions;
import org.json.JSONObject;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;



public final class TestingUtilities {

    private TestingUtilities() {
        // utility class, no objects allowed
    }

    /**
     * Assert if JSON Objects are Equal.
     * @param o1 JSONObject one.
     * @param o2 JSONObject two.
     */
    public static void assertJsonObjectsEqual(JSONObject o1, JSONObject o2) {
        assertThat(o1.toString()).isEqualTo(o2.toString());
    }

    /**
     * Read JSON From File.
     * @param path path of file.
     * @return JSON Object.
     */
    public static JSONObject readJsonFromFile(Path path) {
        return rethrow(() -> new JSONObject(readFile(path)));
    }

    /**
     * Read File from givn path.
     * @param path path of file.
     * @return String content.
     */
    public static String readFile(Path path) {
        return rethrow(() -> new String(readAllBytes(path)));
    }

    /**
     * Create Temporary File.
     * @param content content of file.
     * @return Path.
     */
    public static Path createTemporaryFile(String name, String content) {
        return rethrow(() -> {
            File temp = File.createTempFile(name, ".tmp");
            temp.deleteOnExit();
            Path filePath = Paths.get(temp.toString());
            Files.write(filePath, content.getBytes());
            return filePath;
        });
    }

    /**
     * Exception in test case usually means there is something wrong, it should never be catched, but rather thrown to
     * be handled by JUnit framework.
     */

    public static <T> T rethrow(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param <T> .
     */
    @FunctionalInterface
    interface CheckedSupplier<T> {

        T get() throws Exception;
    }

    /** Assert if Failure Has Info.
     *
     * @param any any object
     * @param msgPart  msg part
     */
    public static void assertFailureHasInfo(Try any, String... msgPart) {
        Java6Assertions.assertThat(any.isFailure()).isTrue();
        AbstractThrowableAssert<?, ? extends Throwable> instance = Java6Assertions.assertThat(any.getCause())
                .hasCauseInstanceOf(Exception.class);
        for (String s : msgPart) {
            instance.hasStackTraceContaining(s);
        }
    }

    /**
     * SSL Builder With TrustStore.
     * @param trustStore path of file.
     * @param pass password.
     * @return SSLContextBuilder.
     */
    public static SSLContextBuilder sslBuilderWithTrustStore(final Path trustStore, final String pass) {
        return rethrow(() ->
                new SSLContextBuilder()
                        .loadTrustMaterial(trustStore.toFile(), pass.toCharArray())
        );
    }

    /**
     * Configure Key Store for testing.
     * @param builder class builder.
     * @param keyStore path of file.
     * @param pass password.
     * @return SSLContextBuilder.
     */
    public static SSLContextBuilder configureKeyStore(
            final SSLContextBuilder builder,
            final Path keyStore,
            final String pass) {
        return rethrow(() -> {
            KeyStore cks = KeyStore.getInstance(KeyStore.getDefaultType());
            cks.load(new FileInputStream(keyStore.toFile()), pass.toCharArray());

            builder.loadKeyMaterial(cks, pass.toCharArray());

            return builder;
        });
    }

    /**
     * Create Rest Template With Ssl.
     * @param context ssl context
     * @return RestTemplate
     */
    public static RestTemplate createRestTemplateWithSsl(final SSLContext context) {
        final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(context);
        final HttpClient httpClient = HttpClients
                .custom()
                .setSSLSocketFactory(socketFactory)
                .build();

        final HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}

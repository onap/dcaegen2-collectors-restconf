/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2022 Huawei. All rights reserved.
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
package org.onap.dcae.common.publishing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.onap.dcae.restapi.ApiException;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.dcae.ApplicationSettings.responseCompatibility;
import static org.onap.dcae.common.publishing.MessageRouterHttpStatusMapper.getHttpStatus;
import static org.onap.dcae.common.publishing.MessageRouterHttpStatusMapper.resolveHttpCode;
import static org.onap.dcae.common.publishing.MessageRouterHttpStatusMapper.responseBody;

class MessageRouterHttpStatusMapperTest {

    public static final String BACKWARDS_COMPATIBILITY = "v7.2";
    public static final String BACKWARDS_COMPATIBILITY_NONE = "NONE";

    @Test
    void shouldResponse202() {
        //given
        responseCompatibility = BACKWARDS_COMPATIBILITY;
        MessageRouterPublishResponse messageRouterPublishResponse = mock(MessageRouterPublishResponse.class);
        when(messageRouterPublishResponse.successful()).thenReturn(true);

        //when
        HttpStatus httpStatusResponse = getHttpStatus(messageRouterPublishResponse);

        //then
        assertSame(HttpStatus.ACCEPTED, httpStatusResponse);
    }

    @Test
    void shouldResponse202Test() {
        //given
        responseCompatibility = BACKWARDS_COMPATIBILITY;
        MessageRouterPublishResponse messageRouterPublishResponse = mock(MessageRouterPublishResponse.class);
        when(messageRouterPublishResponse.successful()).thenReturn(true);

        //when
        HttpStatus httpStatusResponse = getHttpStatus(messageRouterPublishResponse);

        //then
        assertSame(HttpStatus.ACCEPTED, httpStatusResponse);
    }

    @Test
    void shouldResponse200WhenBackwardsCompatibilityIsNone() {
        //given
        responseCompatibility = BACKWARDS_COMPATIBILITY_NONE;
        MessageRouterPublishResponse messageRouterPublishResponse = mock(MessageRouterPublishResponse.class);
        when(messageRouterPublishResponse.successful()).thenReturn(true);

        //when
        HttpStatus httpStatusResponse = getHttpStatus(messageRouterPublishResponse);

        //then
        assertSame(HttpStatus.OK, httpStatusResponse);
    }

    @ParameterizedTest
    @EnumSource(
            value = HttpStatus.class,
            names = {"NOT_FOUND", "REQUEST_TIMEOUT", "TOO_MANY_REQUESTS", "INTERNAL_SERVER_ERROR", "BAD_GATEWAY",
                    "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"}
    )
    void shouldMapErrorsTo503WhenBackwardsCompatibilityIsNone(HttpStatus httpStatus) {
        //given
        responseCompatibility = BACKWARDS_COMPATIBILITY_NONE;
        MessageRouterPublishResponse messageRouterPublishResponse = mock(MessageRouterPublishResponse.class);
        when(messageRouterPublishResponse.failReason()).thenReturn(httpStatus.toString());

        //when
        //then
        assertThrows(RuntimeException.class,()->getHttpStatus(messageRouterPublishResponse));
    }

    @Test
    public void responseBodyTest () throws Exception {
        ApiException result = responseBody("404");
        assertEquals(404, result.httpStatusCode);
        result = responseBody("408");
        assertEquals(408, result.httpStatusCode);
        result = responseBody("429");
        assertEquals(429, result.httpStatusCode);
        result = responseBody("502");
        assertEquals(502, result.httpStatusCode);
        result = responseBody("503");
        assertEquals(503, result.httpStatusCode);
        result = responseBody("504");
        assertEquals(504, result.httpStatusCode);
        result = responseBody("test");
        assertEquals(500, result.httpStatusCode);
    }

    @Test
    public void resolveHttpCodeTest () throws Exception {
        MessageRouterPublishResponse messageRouterPublishResponse = mock(MessageRouterPublishResponse.class);
        when(messageRouterPublishResponse.failReason()).thenReturn("error code");
        String result = resolveHttpCode(messageRouterPublishResponse);
        assertNotNull(result);
    }
}

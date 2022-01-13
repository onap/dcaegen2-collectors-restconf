/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved. 
 * Copyright (C) 2022-2023 Huawei. All rights reserved.
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
package org.onap.dcae.restapi;

import com.google.common.base.CaseFormat;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pawel Szalapski (pawel.szalapski@nokia.com)
 */
public enum ApiException {
    
    UNAUTHORIZED_USER(ExceptionType.POLICY_EXCEPTION, "POL2000", "Unauthorized user", 401),
    NOT_FOUND(ExceptionType.SERVICE_EXCEPTION, "SVC2000", "The following service error occurred: %1. Error code is %2", List.of("Not Found","404"), 404),
    REQUEST_TIMEOUT(ExceptionType.SERVICE_EXCEPTION, "SVC2000", "The following service error occurred: %1. Error code is %2", List.of("Request Timeout","408"), 408),
    TOO_MANY_REQUESTS(ExceptionType.SERVICE_EXCEPTION, "SVC2000", "The following service error occurred: %1. Error code is %2", List.of("Too Many Requests","429"), 429),
    INTERNAL_SERVER_ERROR(ExceptionType.SERVICE_EXCEPTION, "SVC2000", "The following service error occurred: %1. Error code is %2", List.of("Internal Server Error","500"), 500),
    BAD_GATEWAY(ExceptionType.SERVICE_EXCEPTION, "SVC2000", "The following service error occurred: %1. Error code is %2", List.of("Bad Gateway","502"), 502),
    SERVICE_UNAVAILABLE(ExceptionType.SERVICE_EXCEPTION, "SVC2000", "The following service error occurred: %1. Error code is %2", List.of("Service Unavailable","503"), 503),
    GATEWAY_TIMEOUT(ExceptionType.SERVICE_EXCEPTION, "SVC2000", "The following service error occurred: %1. Error code is %2", List.of("Gateway Timeout","504"), 504);

    public final int httpStatusCode;
    private final ExceptionType type;
    private final String code;
    private final String details;
    private final List<String> variables;

    ApiException(ExceptionType type, String code, String details, int httpStatusCode) {
        this(type, code, details, new ArrayList<>(), httpStatusCode);
    }

    ApiException(ExceptionType type, String code, String details, List<String> variables, int httpStatusCode) {
        this.type = type;
        this.code = code;
        this.details = details;
        this.variables = variables;
        this.httpStatusCode = httpStatusCode;
    }

    public JSONObject toJSON() {
        JSONObject exceptionTypeNode = new JSONObject();
        exceptionTypeNode.put("messageId", code);
        exceptionTypeNode.put("text", details);
        if(!variables.isEmpty()) {
            exceptionTypeNode.put("variables", variables);
        }

        JSONObject requestErrorNode = new JSONObject();
        requestErrorNode.put(type.toString(), exceptionTypeNode);

        JSONObject rootNode = new JSONObject();
        rootNode.put("requestError", requestErrorNode);
        return rootNode;
    }

    public enum ExceptionType {
        SERVICE_EXCEPTION, POLICY_EXCEPTION;

        @Override
        public String toString() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
        }
    }

}

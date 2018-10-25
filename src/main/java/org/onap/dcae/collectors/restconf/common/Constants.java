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

public class Constants {
    public static final String KDEFAULT_TEMP_FILENAME = "templateFileName";
    public static final String KSETTING_REST_API_URL = "restapiUrl";
    public static final String KSETTING_HTTP_METHOD = "httpMethod";
    public static final String KSETTING_RESP_PREFIX = "responsePrefix";
    public static final String KSETTING_SKIP_SENDING = "skipSending";
    public static final String KSETTING_FORMAT = "format";
    public static final String KSETTING_DMAAPCONFIGS = "collector.dmaapfile";
    public static final String[] KDEFAULT_DMAAPCONFIGS = new String[]{"./etc/DmaapConfig.json"};
    public static final String KSETTING_SSE_CONNECT_URL = "sseConnectURL";
    public static final int KDEFAULT_MAXQUEUEDEVENTS = 1024 * 4;
    public static final String RESPONSE_CODE = "restapi-result.response-code";
    public static final String OUTPUT_IDENTIFIER = "restapi-result.ietf-subscribed-notifications:output.identifier";
    public static final String RESPONSE_CODE_200 = "200";
    public static final String KCONFIG = "c";
    public static final String KSETTING_UNAME = "restapiUser";
    public static final String KSETTING_PASSWORD = "restapiPassword";
    public static final String KSETTING_TRUST_STORE_FILENAME = "trustStoreFileName";
    public static final String KSETTING_TRUST_STORE_PASSWORD = "trustStorePassword";
    public static final String KSETTING_KEY_STORE_FILENAME = "keyStoreFileName";
    public static final String KSETTING_KEY_STORE_PASSWORD = "keyStorePassword";
}

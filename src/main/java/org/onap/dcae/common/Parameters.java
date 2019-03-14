/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2019 Huawei. All rights reserved.
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

import java.util.Set;

public class Parameters {
    public String templateFileName;
    public String restapiUrl;
    public String restapiUser;
    public String restapiPassword;
    public Format format;
    public String contentType;
    public HttpMethod httpMethod;
    public String responsePrefix;
    public Set<String> listNameList;
    public boolean skipSending;
    public boolean convertResponse;
    public String keyStoreFileName;
    public String keyStorePassword;
    public String trustStoreFileName;
    public String trustStorePassword;
    public boolean ssl;
    public String customHttpHeaders;
    public String partner;
    public Boolean dumpHeaders;
    public String requestBody;
    public String oAuthConsumerKey;
    public String oAuthConsumerSecret;
    public String oAuthSignatureMethod;
    public String oAuthVersion;
    public AuthType authtype;
    public Boolean returnRequestPayload;
}

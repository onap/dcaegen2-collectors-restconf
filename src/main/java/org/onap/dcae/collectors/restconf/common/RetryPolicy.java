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

public class RetryPolicy {
    private String[] hostnames;
    private Integer maximumRetries;

    public Integer getMaximumRetries() {
        return maximumRetries;
    }

    public String getNextHostName(String uri) throws RetryException {
        Integer position = null;

        for (int i = 0; i < hostnames.length; i++) {
            if (uri.contains(hostnames[i])) {
                position = i;
                break;
            }
        }

        if (position == null) {
            throw new RetryException("No match found for the provided uri[" + uri + "] " +
                                             "so the next host name could not be retreived");
        }
        position++;

        if (position > hostnames.length - 1) {
            position = 0;
        }
        return hostnames[position];
    }

    public RetryPolicy(String[] hostnames, Integer maximumRetries) {
        this.hostnames = hostnames;
        this.maximumRetries = maximumRetries;
    }

}

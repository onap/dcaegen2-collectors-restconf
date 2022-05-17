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

package org.onap.dcae;

import org.junit.Test;

import javax.naming.ConfigurationException;

public class ApplicationExceptionTest {
    @Test
    public void TestApplicationException() {
        Exception ex = new Exception();
        ConfigurationException confEx = new ConfigurationException();
        ApplicationException example = new ApplicationException("Exception");
        ApplicationException example1 = new ApplicationException("Exception", ex);
        ApplicationException example2 = new ApplicationException(ex);
        ApplicationException example3 = new ApplicationException(confEx);
    }
}

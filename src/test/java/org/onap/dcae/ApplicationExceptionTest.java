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

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationExceptionTest {
    @Test
    public void TestApplicationException() {
        ConfigurationException confEx = new ConfigurationException();
        ApplicationException example3 = new ApplicationException(confEx);
        Exception ex = new Exception();
        ApplicationException example1 = new ApplicationException("Exception", ex);
        ApplicationException example2 = new ApplicationException(ex);
        ApplicationException example = new ApplicationException("Exception");
        assertTrue(example3.getMessage().contains("ConfigurationException"));
        assertEquals(example1.getMessage(), "Exception");
        assertEquals(example2.getMessage(), "java.lang.Exception");
        assertEquals(example.getMessage(), "Exception");
    }
}

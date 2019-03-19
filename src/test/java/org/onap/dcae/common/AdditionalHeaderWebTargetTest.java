/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
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

package org.onap.dcae.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.WebTarget;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AdditionalHeaderWebTargetTest {


    @Test
    public void  AdditionalHaderWebTargettestBase() {
        WebTarget target = mock(WebTarget.class);
        AdditionalHeaderWebTarget t =  new AdditionalHeaderWebTarget(target, "aaa112", "someheader");
        t.getConfiguration();
        t.getUri();
        t.getUriBuilder();
        t.path("");
        t.register(AdditionalHeaderWebTarget.class);
        t.register(AdditionalHeaderWebTarget.class,0);
        t.register(AdditionalHeaderWebTarget.class, AdditionalHeaderWebTarget.class);
        Object obj = new Object();
        t.register(obj);
        t.register(obj, 0);
        t.register(obj, AdditionalHeaderWebTarget.class);
    }
}
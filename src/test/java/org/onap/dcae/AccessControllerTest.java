/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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

import io.vavr.collection.Map;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.dcae.common.RestConfContext;
import org.onap.dcae.common.RestapiCallNode;
import org.onap.dcae.controller.AccessController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONObject;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.onap.dcae.CLIUtils.processCmdLine;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AccessControllerTest {
    @Mock
    private ApplicationSettings properties;

    @Mock
    private RestConfContext ctx;

    @Mock
    private RestapiCallNode restApiCallNode;

    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path KEYSTORE = Paths.get(RESOURCES.toString(), "keystore");
    protected static final Path KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "passwordfile");
    protected static final Path TRUSTSTORE = Paths.get(RESOURCES.toString(), "truststore");
    protected static final Path TRUSTSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "trustpasswordfile");
    protected static final Path RCC_KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "rcc_passwordfile");
    protected static final Path RCC_KEYSTORE = Paths.get(RESOURCES.toString(), "sdnc.p12");

    @Test
    public void createAndGetAccessControler() {
        when(properties.truststoreFileLocation()).thenReturn(TRUSTSTORE.toString());
        when(properties.truststorePasswordFileLocation()).thenReturn(TRUSTSTORE_PASSWORD_FILE.toString());
        when(properties.keystoreFileLocation()).thenReturn(KEYSTORE.toString());
        when(properties.keystorePasswordFileLocation()).thenReturn(KEYSTORE_PASSWORD_FILE.toString());
        when(properties.rcc_keystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
        when(properties.rcc_keystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
        JSONObject controller = new JSONObject("{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\",\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\",\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\",\"controller_accessTokenFile\":\"./etc/access-token.json\",\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\",\"controller_subscriptionUrl\":\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\",\"event_details\":[{\"event_name\":\"ONT_registration\",\"event_description\":\"ONTregistartionevent\",\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\",\"event_sseventsUrl\":\"null\",\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\",\"event_unSubscriptionTemplate\":\"./etc/ont_registartion_unsubscription_template.json\",\"event_ruleId\":\"777777777\"}]}");
        AccessController acClr = new AccessController(controller,
                properties);

        // then
        assertEquals(acClr.getCfgInfo().getController_name(), "AccessM&C");
        assertEquals(acClr.getCfgInfo().getController_accessTokenMethod(), "put");
        assertEquals(acClr.getCfgInfo().getController_subsMethod(), "post");
        assertEquals(acClr.getCfgInfo().getController_subscriptionUrl(), "/restconf/v1/operations/huawei-nce-notification-action:establish-subscription");
    }

    @Test
    public void accessControllerSuccessfullyActivated() {
        when(properties.truststoreFileLocation()).thenReturn(TRUSTSTORE.toString());
        when(properties.truststorePasswordFileLocation()).thenReturn(TRUSTSTORE_PASSWORD_FILE.toString());
        when(properties.keystoreFileLocation()).thenReturn(KEYSTORE.toString());
        when(properties.keystorePasswordFileLocation()).thenReturn(KEYSTORE_PASSWORD_FILE.toString());
        when(properties.rcc_keystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
        when(properties.rcc_keystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
        when(properties.rcc_policy()).thenReturn("[{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\",\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\",\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\",\"controller_accessTokenFile\":\"./etc/access-token.json\",\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\",\"controller_subscriptionUrl\":\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\",\"event_details\":[{\"event_name\":\"ONT_registration\",\"event_description\":\"ONTregistartionevent\",\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\",\"event_sseventsUrl\":\"null\",\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\",\"event_unSubscriptionTemplate\":\"./etc/ont_registartion_unsubscription_template.json\",\"event_ruleId\":\"777777777\"}]}]");
        when(ctx.getAttribute("responsePrefix.httpResponse")).thenReturn("{\"accessSession\" : \"1234567890\",\"result\" : \"Ok\"}");
        try {
            doThrow(new Exception()).when(restApiCallNode).sendRequest(null, null, 0);
        } catch (Exception e){}

        JSONObject controller = new JSONObject("{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\",\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\",\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\",\"controller_accessTokenFile\":\"./etc/access-token.json\",\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\",\"controller_subscriptionUrl\":\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\",\"event_details\":[{\"event_name\":\"ONT_registration\",\"event_description\":\"ONTregistartionevent\",\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\",\"event_sseventsUrl\":\"null\",\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\",\"event_unSubscriptionTemplate\":\"./etc/ont_registartion_unsubscription_template.json\",\"event_ruleId\":\"777777777\"}]}");
        AccessController acClr = new AccessController(controller,
                properties);

        // then
        acClr.activate();
    }
}
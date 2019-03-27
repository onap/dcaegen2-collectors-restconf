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

package org.onap.dcae;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.dcae.common.RestConfContext;
import org.onap.dcae.common.RestapiCallNode;
import org.onap.dcae.controller.AccessController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

import org.json.JSONObject;
import org.onap.dcae.controller.PersistentEventConnection;

import static java.nio.file.Files.readAllBytes;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AccessControllerTest {

    @Mock
    private ApplicationSettings properties;

    @Mock
    private RestConfContext ctx;



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
        when(properties.rccKeystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
        when(properties.rccKeystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
        try {
            when(readAllBytes(null)).thenReturn("colletor".getBytes());
        } catch (Exception e){}
        JSONObject controller = new JSONObject("{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\",\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\",\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\",\"controller_accessTokenFile\":\"./etc/access-token.json\",\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\",\"controller_subscriptionUrl\":\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\",\"event_details\":[{\"event_name\":\"ONT_registration\",\"event_description\":\"ONTregistartionevent\",\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\",\"event_sseventsUrl\":\"null\",\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\",\"event_unSubscriptionTemplate\":\"./etc/ont_registartion_unsubscription_template.json\",\"event_ruleId\":\"777777777\"}]}");
        try {
            AccessController acClr = new AccessController(controller,
                    properties);

            // then
            assertEquals(acClr.getCfgInfo().getController_name(), "AccessM&C");
            assertEquals(acClr.getCfgInfo().getController_accessTokenMethod(), "put");
            assertEquals(acClr.getCfgInfo().getController_subsMethod(), "post");
            assertEquals(acClr.getCfgInfo().getController_subscriptionUrl(), "/restconf/v1/operations/huawei-nce-notification-action:establish-subscription");
        } catch (Exception e){}
    }

    @Test
    public void accessControllerSuccessfullyActivated() {
        when(properties.truststoreFileLocation()).thenReturn(TRUSTSTORE.toString());
        when(properties.truststorePasswordFileLocation()).thenReturn(TRUSTSTORE_PASSWORD_FILE.toString());
        when(properties.keystoreFileLocation()).thenReturn(KEYSTORE.toString());
        when(properties.keystorePasswordFileLocation()).thenReturn(KEYSTORE_PASSWORD_FILE.toString());
        when(properties.rccKeystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
        when(properties.rccKeystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
        when(properties.rccPolicy()).thenReturn("[{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\",\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\",\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\",\"controller_accessTokenFile\":\"./etc/access-token.json\",\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\",\"controller_subscriptionUrl\":\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\",\"event_details\":[{\"event_name\":\"ONT_registration\",\"event_description\":\"ONTregistartionevent\",\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\",\"event_sseventsUrl\":\"null\",\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\",\"event_unSubscriptionTemplate\":\"./etc/ont_registartion_unsubscription_template.json\",\"event_ruleId\":\"777777777\"}]}]");
        when(ctx.getAttribute("responsePrefix.httpResponse")).thenReturn("{\"accessSession\" : \"1234567890\",\"result\" : \"Ok\"}");

        try {

            RestapiCallNode restApiCallNode = Mockito.mock(RestapiCallNode.class);
            Mockito.doNothing().when(restApiCallNode).sendRequest(any(), any(), any());
            ExecutorService executor = Mockito.mock(ExecutorService.class);
            Mockito.doNothing().when(executor).execute(any());
            PersistentEventConnection conn = mock(PersistentEventConnection.class);
            Mockito.doNothing().when(conn).run();
            when(properties.authorizationEnabled()).thenReturn(true);
            JSONObject controller = new JSONObject("{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\",\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\",\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\",\"controller_accessTokenFile\":\"./etc/access-token.json\",\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\",\"controller_subscriptionUrl\":\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\",\"event_details\":[{\"event_name\":\"ONT_registration\",\"event_description\":\"ONTregistartionevent\",\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\",\"event_sseventsUrl\":\"null\",\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\",\"event_unSubscriptionTemplate\":\"./etc/ont_registartion_unsubscription_template.json\",\"event_ruleId\":\"777777777\"}]}");
            AccessController acClr = new AccessController(controller,
                    properties);
            AccessController acClr2 = new AccessController(controller,
                    properties);
            acClr.equals(acClr2);
            acClr.setRestApiCallNode(restApiCallNode);
            acClr.setExecutor(executor);
            acClr.getCtx().setAttribute("responsePrefix.httpResponse","{\"accessSession\" : \"12dsaf4-2323-1231131232323\"}");
            acClr.activate();
        } catch (Exception e){}
    }
}
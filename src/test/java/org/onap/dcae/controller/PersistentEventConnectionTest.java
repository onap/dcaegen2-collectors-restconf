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
package org.onap.dcae.controller;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.dcae.ApplicationSettings;
import org.onap.dcae.common.RestConfContext;
import org.onap.dcae.common.RestapiCallNode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PersistentEventConnectionTest {

    @Mock
    private ApplicationSettings properties;


    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path KEYSTORE = Paths.get(RESOURCES.toString(), "keystore");
    protected static final Path KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "passwordfile");
    protected static final Path TRUSTSTORE = Paths.get(RESOURCES.toString(), "truststore");
    protected static final Path TRUSTSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "trustpasswordfile");
    protected static final Path RCC_KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "rcc_passwordfile");
    protected static final Path RCC_KEYSTORE = Paths.get(RESOURCES.toString(), "sdnc.p12");

    @Test
    public void runTest() {
        try {
            when(properties.truststoreFileLocation()).thenReturn(TRUSTSTORE.toString());
            when(properties.truststorePasswordFileLocation()).thenReturn(TRUSTSTORE_PASSWORD_FILE.toString());
            when(properties.keystoreFileLocation()).thenReturn(KEYSTORE.toString());
            when(properties.keystorePasswordFileLocation()).thenReturn(KEYSTORE_PASSWORD_FILE.toString());
            when(properties.rccKeystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
            when(properties.rccKeystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
            when(properties.controllerConfigFileLocation())
                    .thenReturn(Paths.get("etc/ont_config.json").toAbsolutePath().toString());

            RestapiCallNode restApiCallNode = Mockito.mock(RestapiCallNode.class);
            Mockito.doNothing().when(restApiCallNode).sendRequest(any(), any(), any());

            JSONObject controller = new JSONObject(
                    "{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\","
                            + "\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\","
                            + "\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\","
                            + "\"controller_accessTokenFile\":\"./etc/access-token.json\","
                            + "\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\","
                            + "\"controller_subscriptionUrl\":"
                            + "\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\","
                            + "\"controller_disableSsl\":\"true\",\"event_details\":[{\"event_name\":"
                            + "\"ONT_registration\",\"event_description\":\"ONTregistartionevent\","
                            + "\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\","
                            + "\"event_sseventsUrl\":\"null\","
                            + "\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\","
                            + "\"event_unSubscriptionTemplate\":"
                            + "\"./etc/ont_registartion_unsubscription_template.json\","
                            + "\"event_ruleId\":\"777777777\"}]}");
            AccessController acClr = new AccessController(controller, properties);


            PersistentEventConnection perEvtConnBuild =
                    new PersistentEventConnection.PersistentEventConnectionBuilder().setEventName("test_event")
                            .setEventDescription("test_description").setEventRuleId("1234646346")
                            .setEventSseventsField("test").setEventSseventsUrl("test")
                            .setEventSseventUrlEmbed(true).setEventSubscriptionTemplate("test")
                            .setEventUnSubscriptionTemplate("test").setParentCtrllr(acClr)
                            .setModifyEvent(true).setModifyMethod("modifyOntEvent")
                            .setUserData("remote_id=AC9.0234.0337;svlan=1001;macAddress=00:11:22:33:44:55;")
                            .createPersistentEventConnection();
            perEvtConnBuild.modifyEventParamMap("restapiUrl", "10.118.191.43:26335");
            assertNotNull(perEvtConnBuild);

            String evtParamVal = perEvtConnBuild.getEventParamMapValue("restapiUrl");
            assertEquals("10.118.191.43:26335", evtParamVal);

            perEvtConnBuild.subscribe();
            perEvtConnBuild.OpenSseConnection();

            String evtRuleId = perEvtConnBuild.getEventRuleId();
            assertEquals("1234646346", evtRuleId);

            String modifyMethod = perEvtConnBuild.getModifyMethod();
            assertEquals("modifyOntEvent", modifyMethod);

            AccessController parentCtrl = perEvtConnBuild.getParentCtrllr();
            assertNotNull(parentCtrl);

            String userData = perEvtConnBuild.getUserData();
            assertEquals("remote_id=AC9.0234.0337;svlan=1001;macAddress=00:11:22:33:44:55;", userData);

            RestConfContext contextData = perEvtConnBuild.getCtx();
            assertNotNull(contextData);

            Map<String, String> evtParaMap = perEvtConnBuild.getEventParaMap();
            assertNotNull("10.118.191.43:26335", evtParaMap.get("restapiUrl"));

            perEvtConnBuild.shutdown();

            boolean isModify = perEvtConnBuild.isModifyEvent();
            assertTrue(isModify);
        } catch (Exception ex) {
            System.out.println("Exception " + ex);
        }
    }
}

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.dcae.ApplicationSettings;
import org.onap.dcae.RestConfCollector;
import org.onap.dcae.common.EventData;
import org.onap.dcae.common.RestConfContext;
import org.onap.dcae.common.publishing.DMaaPConfigurationParser;
import org.onap.dcae.common.publishing.EventPublisher;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PersistentEventConnectionTest {

    @Mock
    private ApplicationSettings properties;

    private EventPublisher eventPublisher;

    Path path = Paths.get("src/test/resources/testParseDMaaPCredentialsLegacy.json");
    java.util.Map<String, String[]> streamMap;
    protected static final Path RESOURCES = Paths.get("src", "test", "resources");
    protected static final Path KEYSTORE = Paths.get(RESOURCES.toString(), "keystore");
    protected static final Path KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "passwordfile");
    protected static final Path TRUSTSTORE = Paths.get(RESOURCES.toString(), "truststore");
    protected static final Path TRUSTSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "trustpasswordfile");
    protected static final Path RCC_KEYSTORE_PASSWORD_FILE = Paths.get(RESOURCES.toString(), "rcc_passwordfile");
    protected static final Path RCC_KEYSTORE = Paths.get(RESOURCES.toString(), "sdnc.p12");


    PersistentEventConnection.PersistentEventConnectionBuilder eventConnectionBuilder = new PersistentEventConnection.PersistentEventConnectionBuilder();
    PersistentEventConnection eventConnection;
    JSONObject controller;
    AccessController acClr;
    EventData eventData;

    /**
     * set up before testcase.
     */
    @Before
    public void setUp() {
        eventPublisher = EventPublisher.createPublisher(LoggerFactory.getLogger("some_log"),
                DMaaPConfigurationParser.parseToDomainMapping(path).get());
        streamMap = RestConfCollector.parseStreamIdToStreamHashMapping("notification=device-registration");

        when(properties.truststoreFileLocation()).thenReturn(TRUSTSTORE.toString());
        when(properties.truststorePasswordFileLocation()).thenReturn(TRUSTSTORE_PASSWORD_FILE.toString());
        when(properties.keystoreFileLocation()).thenReturn(KEYSTORE.toString());
        when(properties.keystorePasswordFileLocation()).thenReturn(KEYSTORE_PASSWORD_FILE.toString());
        when(properties.rccKeystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
        when(properties.rccKeystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
        when(properties.controllerConfigFileLocation())
                .thenReturn(Paths.get("etc/ont_config.json").toAbsolutePath().toString());

        controller = new JSONObject(
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

        acClr = new AccessController(controller, properties);

        eventConnection = new PersistentEventConnection.PersistentEventConnectionBuilder()
            .setEventName("")
            .setEventDescription("").setEventSseventUrlEmbed(true).setEventSseventsField("")
            .setEventSseventsUrl("")
            .setEventSubscriptionTemplate("").setEventUnSubscriptionTemplate("").setEventRuleId("1234646346")
            .setParentCtrllr(acClr).setModifyEvent(true).setModifyMethod("modifyOntEvent")
            .setUserData("remote_id=AC9.0234.0337;svlan=1001;macAddress=00:11:22:33:44:55;")
            .createPersistentEventConnection();
        eventData = new EventData(eventConnection, new JSONObject("{\n"
                + "  \"notification\" : {\n"
                + "    \"notification-id\" : \"01010101011\",\n"
                + "    \"event-time\" : \"2019-3-9T3:30:30.547z\",\n"
                + "    \"message\" : {\n"
                + "      \"object-type\" : \"onu\",\n"
                + "      \"topic\" : \"resources\",\n"
                + "      \"version\" : \"v1\",\n"
                + "      \"operation\" : \"create\",\n"
                + "      \"content\" : {\n"
                + "        \"onu\" : {\n"
                + "          \"alias\" : \"\",\n"
                + "          \"refParentLTP\" : \"gpon.0.5.1\",\n"
                + "          \"sn\" : \"HWTCC01B7503\",\n"
                + "          \"refParentLTPNativeId\" : \"NE=167772165,FR=0,S=5,CP=-1,PP=||1|\",\n"
                + "          \"onuId\": \"\",\n"
                + "          \"refParentNE\" : \"aaaaaaaaa-aaaaa-aaaa-aaaa-aaa167772165\",\n"
                + "          \"refParentNeNativeId\": \"NE=167772165\"\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}"));
    }


    @Test
    public void setNameTest() {
        eventConnectionBuilder.setEventName("test");
    }

    @Test
    public void setEventNameTest() {
        eventConnectionBuilder.setEventDescription("test description");
    }

    @Test
    public void setEventSseventUrlEmbedTest() {
        eventConnectionBuilder.setEventSseventUrlEmbed(true);
    }

    @Test
    public void setEventSseventsFieldTest() {
        eventConnectionBuilder.setEventSseventsField("test");
    }

    @Test
    public void setEventSseventsUrlTest() {
        eventConnectionBuilder.setEventSseventsUrl("");
    }

    @Test
    public void setEventSubscriptionTemplateTest() {
        eventConnectionBuilder.setEventSubscriptionTemplate("");
    }

    @Test
    public void setEventUnSubscriptionTemplateTest() {
        eventConnectionBuilder.setEventUnSubscriptionTemplate("");
    }

    @Test
    public void setEventRuleIdTest() {
        eventConnectionBuilder.setEventRuleId("");
    }

    @Test
    public void setParentCtrllrTest() {
        eventConnectionBuilder.setParentCtrllr(acClr);
    }

    @Test
    public void setModifyEventTest() {
        eventConnectionBuilder.setModifyEvent(false);
    }

    @Test
    public void setModifyMethodTest() {
        eventConnectionBuilder.setModifyMethod("");
    }

    @Test
    public void setUserDataTest() {
        eventConnectionBuilder.setUserData("");
    }

    @Test
    public void createConnectionTest() {
        PersistentEventConnection evtCon = new PersistentEventConnection.PersistentEventConnectionBuilder()
            .setEventName("")
            .setEventDescription("").setEventSseventUrlEmbed(true).setEventSseventsField("")
            .setEventSseventsUrl("")
            .setEventSubscriptionTemplate("").setEventUnSubscriptionTemplate("").setEventRuleId("1234646346")
            .setParentCtrllr(acClr).setModifyEvent(true).setModifyMethod("modifyOntEvent")
            .setUserData("remote_id=AC9.0234.0337;svlan=1001;macAddress=00:11:22:33:44:55;")
            .createPersistentEventConnection();
    }

    @Test
    public void runTest() {
        eventConnection.run();
    }

    @Test
    public void getEventRuleIdTest() {
        String eventRuleId = eventData.getConn().getEventRuleId();
        String paramMapValue = eventData.getConn().getEventParamMapValue("");
        String modifyMethod = eventData.getConn().getModifyMethod();
        String userData = eventData.getConn().getUserData();
        boolean modifyEvt = eventData.getConn().isModifyEvent();
        eventData.getConn().modifyEventParamMap("","");
        AccessController actrl = eventData.getConn().getParentCtrllr();
        eventData.getConn().printEventParamMap();
        RestConfContext restConfContext = eventData.getConn().getCtx();
        Map<String, String> paraMap = eventData.getConn().getEventParaMap();
        eventData.getConn().shutdown();

        assertEquals("1234646346", eventRuleId);
        assertEquals("modifyOntEvent",modifyMethod);
        assertEquals("remote_id=AC9.0234.0337;svlan=1001;macAddress=00:11:22:33:44:55;", userData);
        assertEquals(acClr, actrl);
        assertEquals(true, modifyEvt);
    }
}

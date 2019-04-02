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



import org.json.JSONObject;
import org.junit.Before;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.dcae.ApplicationSettings;
import org.onap.dcae.RestConfCollector;
import org.onap.dcae.common.publishing.DMaaPConfigurationParser;
import org.onap.dcae.common.publishing.EventPublisher;
import org.onap.dcae.controller.AccessController;
import org.onap.dcae.controller.PersistentEventConnection;
import org.slf4j.LoggerFactory;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;



import static io.vavr.API.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.onap.dcae.common.publishing.DMaaPConfigurationParser.parseToDomainMapping;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EventProcessorTest {



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

    @Before
    public void setUp() {
        eventPublisher = EventPublisher.createPublisher(LoggerFactory.getLogger("some_log"), DMaaPConfigurationParser.
                parseToDomainMapping(path).get());
        streamMap = RestConfCollector.parseStreamIdToStreamHashMapping("notification=device-registration");

    }

    @Test
    public void testEventProcessorRunException() {
        when(properties.truststoreFileLocation()).thenReturn(TRUSTSTORE.toString());
        when(properties.truststorePasswordFileLocation()).thenReturn(TRUSTSTORE_PASSWORD_FILE.toString());
        when(properties.keystoreFileLocation()).thenReturn(KEYSTORE.toString());
        when(properties.keystorePasswordFileLocation()).thenReturn(KEYSTORE_PASSWORD_FILE.toString());
        when(properties.rccKeystoreFileLocation()).thenReturn(RCC_KEYSTORE.toString());
        when(properties.rccKeystorePasswordFileLocation()).thenReturn(RCC_KEYSTORE_PASSWORD_FILE.toString());
        JSONObject controller = new JSONObject(
                "{\"controller_name\":\"AccessM&C\",\"controller_restapiUrl\":\"10.118.191.43:26335\",\"controller_restapiUser\":\"access\",\"controller_restapiPassword\":\"Huawei@123\",\"controller_accessTokenUrl\":\"/rest/plat/smapp/v1/oauth/token\",\"controller_accessTokenFile\":\"./etc/access-token.json\",\"controller_accessTokenMethod\":\"put\",\"controller_subsMethod\":\"post\",\"controller_subscriptionUrl\":\"/restconf/v1/operations/huawei-nce-notification-action:establish-subscription\",\"event_details\":[{\"event_name\":\"ONT_registration\",\"event_description\":\"ONTregistartionevent\",\"event_sseventUrlEmbed\":\"true\",\"event_sseventsField\":\"output.url\",\"event_sseventsUrl\":\"null\",\"event_subscriptionTemplate\":\"./etc/ont_registartion_subscription_template.json\",\"event_unSubscriptionTemplate\":\"./etc/ont_registartion_unsubscription_template.json\",\"event_ruleId\":\"777777777\"}]}");
        AccessController acClr = new AccessController(controller, properties);

        PersistentEventConnection p = new PersistentEventConnection.PersistentEventConnectionBuilder().setEventName("")
                .setEventDescription("").setEventSseventUrlEmbed(true).setEventSseventsField("").setEventSseventsUrl("")
                .setEventSubscriptionTemplate("").setEventUnSubscriptionTemplate("").setEventRuleId("1234646346")
                .setParentCtrllr(acClr).createPersistentEventConnection();
        p.getEventParamMapValue("restapiUrl");
        p.modifyEventParamMap("restapiUrl", "10.118.191.43:26335");
        RestConfCollector.fProcessingInputQueue = new LinkedBlockingQueue<>(4);
        RestConfCollector.fProcessingInputQueue.offer(new EventData(p, new JSONObject("{}")));
        RestConfCollector.fProcessingInputQueue.offer(new EventData(null, null));
        EventProcessor ev = new EventProcessor(eventPublisher, streamMap);
        ev.run();
    }
}
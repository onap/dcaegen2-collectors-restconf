/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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

package org.onap.dcae.collectors.restconf.common.event.publishing;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import io.vavr.control.Try;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DMaaPEventPublisher implements EventPublisher {

    private static final int PENDING_MESSAGE_LOG_THRESHOLD = 100;
    private static final Logger log = LoggerFactory.getLogger(DMaaPEventPublisher.class);
    private final DMaaPPublishersCache publishersCache;
    private final Logger outputLogger;

    DMaaPEventPublisher(DMaaPPublishersCache DMaaPPublishersCache,
                        Logger outputLogger) {
        this.publishersCache = DMaaPPublishersCache;
        this.outputLogger = outputLogger;
    }

    @Override
    public void sendEvent(JSONObject event, String domain) {
        publishersCache.getPublisher(domain)
                .onEmpty(() ->
                                 log.warn(VavrUtils.f("Could not find event publisher for domain: '%s', dropping message: '%s'", domain, event)))
                .forEach(publisher -> sendEvent(event, domain, publisher));
    }

    private void sendEvent(JSONObject event, String domain, CambriaBatchingPublisher publisher) {
        Try.run(() -> uncheckedSendEvent(event, domain, publisher))
                .onFailure(exc -> closePublisher(event, domain, exc));
    }

    private void uncheckedSendEvent(JSONObject event, String domain, CambriaBatchingPublisher publisher)
            throws IOException {
        System.out.println("printing publisher information" + publisher);
        int pendingMsgs = publisher.send("MyPartitionKey", event.toString());
        if (pendingMsgs > PENDING_MESSAGE_LOG_THRESHOLD) {
            log.info("Pending messages count: " + pendingMsgs);
        }
        String infoMsg = VavrUtils.f("Event: '%s' scheduled to be send asynchronously on domain: '%s'", event, domain);
        log.info(infoMsg);
        outputLogger.info(infoMsg);
    }

    private void closePublisher(JSONObject event, String domain, Throwable e) {
        log.error(VavrUtils.f("Unable to schedule event: '%s' on domain: '%s'. Closing publisher and dropping message.",
                              event, domain), e);
        publishersCache.closePublisherFor(domain);
    }

}

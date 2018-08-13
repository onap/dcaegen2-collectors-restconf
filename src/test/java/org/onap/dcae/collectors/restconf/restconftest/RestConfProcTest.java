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

package org.onap.dcae.collectors.restconf.restconftest;

import com.att.nsa.cmdLine.NsaCommandLineUtil;
import com.att.nsa.drumlin.service.framework.DrumlinServlet;
import com.att.nsa.drumlin.till.nv.impl.nvPropertiesFile;
import com.att.nsa.drumlin.till.nv.impl.nvReadableStack;
import com.att.nsa.drumlin.till.nv.impl.nvReadableTable;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.onap.dcae.collectors.restconf.common.Constants;
import org.onap.dcae.collectors.restconf.common.RestConfCollector;
import org.onap.dcae.collectors.restconf.common.RestConfContext;
import org.onap.dcae.collectors.restconf.common.RestConfProc;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RestConfProcTest {

    private static final URI CONTEXT = URI.create("http://localhost:8080/");

    @Test
    public void testEstablishPersistentConnection() throws Exception {

        final Map<String, String> argMap = new HashMap<>();
        final String config = NsaCommandLineUtil.getSetting(argMap, Constants.KCONFIG, "collector.properties");
        final URL settingStream = DrumlinServlet.findStream(config, RestConfCollector.class);

        final nvReadableStack settings = new nvReadableStack();

        settings.push(new nvPropertiesFile(settingStream));
        settings.push(new nvReadableTable(argMap));

        RestConfProc restConfProc = new RestConfProc(settings);

        final ResourceConfig resourceConfig = new ResourceConfig(SseResource.class, SseFeature.class);
        GrizzlyHttpServerFactory.createHttpServer(CONTEXT, resourceConfig);
        RestConfContext ctx = new RestConfContext();
        ctx.setAttribute("prop.encoding-json", "encoding-json");
        ctx.setAttribute("restapi-result.response-code", "200");
        ctx.setAttribute("restapi-result.ietf-subscribed-notifications:output.identifier", "100");

        Map<String, String> p = new HashMap<>();
        p.put("sseConnectURL", "http://localhost:8080/ssevents");
        p.put("subscriberId", "networkId");
        p.put("responsePrefix", "restapi-result");

        restConfProc.establishPersistentConnection(p, ctx);
        Thread.sleep(1000);
    }
}

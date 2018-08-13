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

package org.onap.dcae.collectors.restconf.common;

import com.att.nsa.cmdLine.NsaCommandLineUtil;
import com.att.nsa.drumlin.service.framework.DrumlinServlet;
import com.att.nsa.drumlin.till.nv.impl.nvPropertiesFile;
import com.att.nsa.drumlin.till.nv.impl.nvReadableStack;
import com.att.nsa.drumlin.till.nv.impl.nvReadableTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

public class RestConfCollector {

    public static final Logger eplog = LoggerFactory.getLogger("org.onap.restconf.common.error");

    public static void main(String[] args) {
        try {
            final Map<String, String> argMap = NsaCommandLineUtil.processCmdLine(args, true);
            final String config = NsaCommandLineUtil.getSetting(argMap, Constants.KCONFIG, "collector.properties");
            final URL settingStream = DrumlinServlet.findStream(config, RestConfCollector.class);

            final nvReadableStack settings = new nvReadableStack();
            settings.push(new nvPropertiesFile(settingStream));
            settings.push(new nvReadableTable(argMap));

            RestConfProc restConfProc = new RestConfProc(settings);
            Map<String, String> paraMap = restConfProc.getParaMap();
            String restApiURL = paraMap.get(Constants.KSETTING_REST_API_URL);
            String sseEventsURL = paraMap.get(Constants.KSETTING_SSE_CONNECT_URL);
            String[] listRestApiURL = restApiURL.split(";");
            String[] listSseEventsURL = sseEventsURL.split(";");
            for (int i = 0; i < listRestApiURL.length; i++) {
                paraMap.put(Constants.KSETTING_REST_API_URL, "http://" + listRestApiURL[i] +
                        "/RestConfServer/rest/operations/establish-subscription");
                paraMap.put(Constants.KSETTING_SSE_CONNECT_URL, listSseEventsURL[i]);
                restConfProc.establishSubscription(paraMap, restConfProc.getCtx());
            }

        } catch (Exception e) {
            RestConfCollector.eplog.error("Fatal error during application startup", e);
            throw new RuntimeException(e);
        }
    }
}

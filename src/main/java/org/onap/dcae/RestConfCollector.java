/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved. 
 * Copyright (C) 2022-2023 Huawei. All rights reserved.
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
import org.json.JSONObject;
import org.onap.dcae.common.ControllerActivationState;
import org.onap.dcae.common.EventData;
import org.onap.dcae.common.EventProcessor;
import org.onap.dcae.common.publishing.DMaaPConfigurationParser;
import org.onap.dcae.common.publishing.DMaaPEventPublisher;
import org.onap.dcae.common.publishing.EventPublisher;
import org.onap.dcae.common.publishing.PublisherConfig;
import org.onap.dcae.controller.AccessController;
import org.onap.dcae.controller.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.onap.dcae.configuration.ConfigurationHandler;
import org.onap.dcae.configuration.ConfigUpdater;
import org.onap.dcae.configuration.ConfigUpdaterFactory;
import org.onap.dcae.configuration.cbs.CbsClientConfigurationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication(exclude = {GsonAutoConfiguration.class, SecurityAutoConfiguration.class})
public class RestConfCollector {
    private static final Logger oplog = LoggerFactory.getLogger("org.onap.dcae.common.output");
    private static final int MAX_THREADS = 20;
    private static final int DEFAULT_CONFIGURATION_FETCH_PERIOD = 5;
    private static Logger log = LoggerFactory.getLogger(RestConfCollector.class);
    public static LinkedBlockingQueue<EventData> fProcessingInputQueue;
    private static ApplicationSettings properties;
    private static ConfigurableApplicationContext context;
    private static ConfigLoader configLoader;
    private static SpringApplication app;
    private static ScheduledFuture<?> scheduleFeatures;
    private static ScheduledFuture<?> scheduleCtrlActivation;
    private static ExecutorService executor;
    private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private static ScheduledThreadPoolExecutor scheduledThExController;
    private static EventPublisher eventPublisher;
    private static EventProcessor eventProcessor;
    private static ConfigUpdater configUpdater;
    private static ApplicationConfigurationListener applicationConfigurationListener;

    /* List of Controllers */
    private static java.util.Map<String, AccessController> controllerStore = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        oplog.info("RestconfController starting");
        app = new SpringApplication(RestConfCollector.class);
        properties = new ApplicationSettings(args, CLIUtils::processCmdLine);
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThExController = new ScheduledThreadPoolExecutor(1);
        init();
        applicationConfigurationListener = startListeningForApplicationConfigurationStoredInConsul();
        app.setAddCommandLineProperties(true);
        context = app.run();
        controllerConfig(properties);
        oplog.info("RestConfController running .....");
    }

    private static ApplicationConfigurationListener startListeningForApplicationConfigurationStoredInConsul() {
        ConfigurationHandler cbsHandler = new ConfigurationHandler(new CbsClientConfigurationProvider(), configUpdater);
        ApplicationConfigurationListener applicationConfigProvider = new ApplicationConfigurationListener(Duration.ofMinutes(DEFAULT_CONFIGURATION_FETCH_PERIOD), cbsHandler);

        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.execute(applicationConfigProvider);

        return applicationConfigProvider;
    }

    public static void restartApplication() {
        Thread thread = new Thread(() -> {
            controllerConfigCleanup();
            context.close();
            properties.reloadProperties();
            scheduleFeatures.cancel(true);
            scheduleCtrlActivation.cancel(true);
            init();
            controllerConfig(properties);
            context = SpringApplication.run(RestConfCollector.class);

            configUpdater.setPaths(properties.configurationFileLocation(),
                    Paths.get(properties.dMaaPConfigurationFileLocation()));
            applicationConfigurationListener.reload(Duration.ofMinutes(properties.configurationUpdateFrequency()));
        });
        thread.setDaemon(false);
        thread.start();
    }


    private static void init() {
        fProcessingInputQueue = new LinkedBlockingQueue<>(properties.maximumAllowedQueuedEvents());

        configUpdater = ConfigUpdaterFactory.create(
                properties.configurationFileLocation(),
                Paths.get(properties.dMaaPConfigurationFileLocation()));
        eventPublisher = new DMaaPEventPublisher(getDmapConfig());
    }

    private static Map<String, PublisherConfig> getDmapConfig() {
        return DMaaPConfigurationParser.
                parseToDomainMapping(Paths.get(properties.dMaaPConfigurationFileLocation())).get();
    }

    @Bean
    @Lazy
    public ApplicationSettings applicationSettings() {
        return properties;
    }

    @Bean
    public LinkedBlockingQueue<EventData> inputQueue() {
        return fProcessingInputQueue;
    }

    public static java.util.Map<String, String[]> parseStreamIdToStreamHashMapping(String streamId) {

        java.util.Map<String, String[]> streamidHash = new HashMap<>();
        String[] list = streamId.split("\\|");
        for (String aList : list) {
            String domain = aList.split("=")[0];
            String[] streamIdList = aList.substring(aList.indexOf('=') + 1).split(",");
            streamidHash.put(domain, streamIdList);
            oplog.info("adding domain " + domain + " count" + streamIdList.length);
        }
        return streamidHash;
    }

    private static void controllerConfig(ApplicationSettings properties) {
        oplog.info("Policy received " + properties.rccPolicy());
        if (!properties.rccPolicy().equals("")) {
            JSONArray contollers = new JSONArray(properties.rccPolicy());
            for (int i = 0; i < contollers.length(); i++) {
                JSONObject controller = contollers.getJSONObject(i);
                oplog.info(" object " + controller.toString());
                AccessController acClr = new AccessController(controller,
                        properties);
                controllerStore.put(controller.get("controller_name").toString(), acClr);
                oplog.info("Activating controller " + acClr.getCfgInfo().getController_name());
                acClr.activate();
            }
        }
    }

    private static void controllerConfigCleanup() {
        log.info("controller ConfigCleanup!");
        for (java.util.Map.Entry<String, AccessController> entry : controllerStore.entrySet()) {
            AccessController acstlr = entry.getValue();
            log.info("controller detail " + acstlr.getCfgInfo().getController_restapiUrl());
            acstlr.clearAllPersistentConnectios();
            controllerStore.remove(acstlr);
        }
        controllerStore.clear();
    }

    public static void handleEvents(EventData ev) throws Exception {
        if (!fProcessingInputQueue.offer(ev)) {
            throw new InterruptedException();
        }
        log.info("RestConfCollector.handleEvents:EVENTS has been published successfully!");
    }

    private static EventPublisher getEventPublisher() {
        return EventPublisher.createPublisher(oplog, getDmapConfig());
    }

    private  static class ControllerActivationTask implements Runnable
    {
        public ControllerActivationTask() {
        }

        @Override
        public void run()
        {
            try {
                Iterator<String> it1 = controllerStore.keySet().iterator();
                while(it1.hasNext()){
                    String key = it1.next();
                    AccessController ctlr = controllerStore.get(key);
                    if(ctlr.getState() == ControllerActivationState.INIT) {
                        log.info("Activating controller " + key);
                        ctlr.activate();
                    }
                }
            } catch (Exception e) {
                log.info("Activation failed");
            }
        }
    }
}

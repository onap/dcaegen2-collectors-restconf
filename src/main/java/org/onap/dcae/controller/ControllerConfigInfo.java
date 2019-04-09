/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.restconf
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
package org.onap.dcae.controller;

public class ControllerConfigInfo {
    private String controller_name;
    private String controller_restapiUrl;
    private String controller_restapiUser;
    private String controller_restapiPassword;
    private String controller_accessTokenUrl;
    private String controller_accessTokenFile;
    private String controller_subscriptionUrl;
    private String controller_accessTokenMethod;
    private String controller_subsMethod;



    public static class ControllerConfigInfoBuilder {
        private String controller_name;
        private String controller_restapiUrl;
        private String controller_restapiUser;
        private String controller_restapiPassword;
        private String controller_accessTokenUrl;
        private String controller_accessTokenFile;
        private String controller_subscriptionUrl;
        private String controller_accessTokenMethod;
        private String controller_subsMethod;

        public ControllerConfigInfoBuilder setController_name(String controller_name) {
            this.controller_name = controller_name;
            return this;
        }

        public ControllerConfigInfoBuilder setController_restapiUrl(String controller_restapiUrl) {
            this.controller_restapiUrl = controller_restapiUrl;
            return this;
        }

        public ControllerConfigInfoBuilder setController_restapiUser(String controller_restapiUser) {
            this.controller_restapiUser = controller_restapiUser;
            return this;
        }

        public ControllerConfigInfoBuilder setController_restapiPassword(String controller_restapiPassword) {
            this.controller_restapiPassword = controller_restapiPassword;
            return this;
        }

        public ControllerConfigInfoBuilder setController_accessTokenUrl(String controller_accessTokenUrl) {
            this.controller_accessTokenUrl = controller_accessTokenUrl;
            return this;
        }

        public ControllerConfigInfoBuilder setController_accessTokenFile(String controller_accessTokenFile) {
            this.controller_accessTokenFile = controller_accessTokenFile;
            return this;
        }

        public ControllerConfigInfoBuilder setController_subscriptionUrl(String controller_subscriptionUrl) {
            this.controller_subscriptionUrl = controller_subscriptionUrl;
            return this;
        }

        public ControllerConfigInfoBuilder setController_accessTokenMethod(String controller_accessTokenMethod) {
            this.controller_accessTokenMethod = controller_accessTokenMethod;
            return this;
        }

        public ControllerConfigInfoBuilder setController_subsMethod(String controller_subsMethod) {
            this.controller_subsMethod = controller_subsMethod;
            return this;
        }

        public ControllerConfigInfo createControllerConfigInfo() {
            return new ControllerConfigInfo(this);
        }
    }


    public ControllerConfigInfo(ControllerConfigInfoBuilder controllerConfigInfoBuilder) {
        this.controller_name = controllerConfigInfoBuilder.controller_name;
        this.controller_restapiUrl = controllerConfigInfoBuilder.controller_restapiUrl;
        this.controller_restapiUser = controllerConfigInfoBuilder.controller_restapiUser;
        this.controller_restapiPassword = controllerConfigInfoBuilder.controller_restapiPassword;
        this.controller_accessTokenUrl = controllerConfigInfoBuilder.controller_accessTokenUrl;
        this.controller_accessTokenFile = controllerConfigInfoBuilder.controller_accessTokenFile;
        this.controller_subscriptionUrl = controllerConfigInfoBuilder.controller_subscriptionUrl;
        this.controller_accessTokenMethod = controllerConfigInfoBuilder.controller_accessTokenMethod;
        this.controller_subsMethod = controllerConfigInfoBuilder.controller_subsMethod;

    }


    public String getController_name() {
        return controller_name;
    }

    public String getController_restapiUrl() {
        return controller_restapiUrl;
    }

    public String getController_restapiUser() {
        return controller_restapiUser;
    }

    public String getController_restapiPassword() {
        return controller_restapiPassword;
    }

    public String getController_accessTokenUrl() {
        return controller_accessTokenUrl;
    }

    public String getController_accessTokenFile() {
        return controller_accessTokenFile;
    }

    public String getController_accessTokenMethod() {
        return controller_accessTokenMethod;
    }

    public String getController_subsMethod() {
        return controller_subsMethod;
    }

    public String getController_subscriptionUrl() {
        return controller_subscriptionUrl;
    }
}
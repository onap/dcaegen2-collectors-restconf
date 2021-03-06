/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
package org.onap.dcae.restapi;

import io.vavr.control.Option;
import java.io.IOException;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.onap.dcae.ApplicationSettings;
import org.onap.dcaegen2.services.sdk.security.CryptPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

final class ApiAuthInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiAuthInterceptor.class);
    private final CryptPassword cryptPassword = new CryptPassword();
    private final ApplicationSettings applicationSettings;



    ApiAuthInterceptor(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        if (applicationSettings.authorizationEnabled()) {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !isAuthorized(authorizationHeader)) {
                response.setStatus(400);

                response.getWriter().write(ApiException.UNAUTHORIZED_USER.toJSON().toString());
                return false;
            }
        }
        return true;
    }

    private boolean isAuthorized(String authorizationHeader) {
        try  {
            String encodedData = authorizationHeader.split(" ")[1];
            String decodedData = new String(Base64.getDecoder().decode(encodedData));
            String providedUser = decodedData.split(":")[0].trim();
            String providedPassword = decodedData.split(":")[1].trim();
            Option<String> maybeSavedPassword = applicationSettings.validAuthorizationCredentials().get(providedUser);
            boolean userRegistered = maybeSavedPassword.isDefined();
            return userRegistered && cryptPassword.matches(providedPassword,maybeSavedPassword.get());
        } catch (Exception e) {
            LOG.warn(String.format("Could not check if user is authorized (header: '%s')), probably malformed header.",
                    authorizationHeader), e);
            return false;
        }
    }
}
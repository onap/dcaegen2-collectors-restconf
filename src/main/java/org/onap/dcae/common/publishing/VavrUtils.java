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
package org.onap.dcae.common.publishing;

import io.vavr.API;
import io.vavr.API.Match.Case;
import java.util.function.Consumer;
import org.slf4j.Logger;

import static io.vavr.API.$;

/**
 * @author Pawel Szalapski (pawel.szalapski@nokia.com)
 */
public final class VavrUtils {

    private VavrUtils() {
        /* utils aggregator */
    }

    /**
     * Shortcut for 'string interpolation'
     * @param msg String message
     * @param args var args
     * @return String
     */
    public static String f(String msg, Object... args) {
        return String.format(msg, args);
    }

    /**
     * Wrap failure with a more descriptive message of what has failed and chain original cause. Used to provide a
     * context for errors instead of raw exception.
     * @param msg String message
     * @return Case
     */
    public static Case<Throwable, Throwable> enhanceError(String msg) {
        return API.Case($(), e -> new RuntimeException(msg, e));
    }

    public static Case<Throwable, Throwable> enhanceError(String pattern, Object... arguments) {
        return API.Case($(), e -> new RuntimeException(f(pattern, arguments), e));
    }

    public static Consumer<Throwable> logError(Logger withLogger) {
        return e -> withLogger.error(e.getMessage(), e);
    }


}

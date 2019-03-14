/*
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2019 Huawei. All rights reserved.
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

import java.util.HashMap;

public class CLIUtils {

    public static Map<String, String> processCmdLine(String[] args) {
        final java.util.Map<String, String> map = new HashMap<>();

        String argumentName = null;

        for (String arg : args) {
            if (isArgumentName(arg)) {
                argumentName = resolveArgumentName(arg);
                map.put(argumentName, "");
            } else {
                map.put(argumentName, arg);
            }
        }

        return io.vavr.collection.HashMap.ofAll(map);
    }

    private static String resolveArgumentName(String arg) {
        return arg.substring(1);
    }

    private static boolean isArgumentName(String arg) {
        return arg.startsWith("-");
    }
}

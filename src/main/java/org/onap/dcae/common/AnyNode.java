/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.restconfcollector
 * ================================================================================
 * Copyright (C) 2018 Nokia Networks Intellectual Property. All rights reserved. 
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

import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.stream.StreamSupport;

import static io.vavr.API.Set;

/**
 * This class is a wrapper for 2 most used entities of org.json lib: JSONArray and JSONObject and comprises utility
 * methods for fast access of json structures without need to explicitly coerce between them. While using this, bear in
 * mind it does not contain exception handling - it is assumed that when using, the parsed json structure is known.
 *
 * @author koblosz
 */
public class AnyNode {

    private Object obj;

    private AnyNode(Object object) {
        this.obj = object;
    }

    public static AnyNode fromString(String content) {
        return new AnyNode(new JSONObject(content));
    }

    public Set<String> keys() {
        return Set(asJsonObject().keySet().toArray(new String[]{}));
    }

    public AnyNode get(String key) {
        return new AnyNode(asJsonObject().get(key));
    }

    public String toString() {
        return this.obj.toString();
    }

    public Option<AnyNode> getAsOption(String key) {
        try {
            AnyNode value = get(key);
            if ("null".equals(value.toString())) {
                return Option.none();
            }
            return Option.some(value);
        } catch (JSONException ex) {
            return Option.none();
        }
    }

    public List<AnyNode> toList() {
        return List.ofAll(StreamSupport.stream(((JSONArray) this.obj).spliterator(), false).map(AnyNode::new));
    }

    public boolean has(String key) {
        return !getAsOption(key).isEmpty();
    }

    private JSONObject asJsonObject() {
        return (JSONObject) this.obj;
    }

}

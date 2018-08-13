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

import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.stream.StreamSupport;

import static io.vavr.API.Set;

public class AnyNode {
    private Object obj;

    private AnyNode(Object object) {
        this.obj = object;
    }

    public static AnyNode fromString(String content) {
        return new AnyNode(new JSONObject(content));
    }

    /**
     * Returns key set of underlying object. It is assumed that underlying object is of type org.json.JSONObject.
     *
     * @return key set of underlying objects
     */
    public Set<String> keys() {
        return Set(asJsonObject().keySet().toArray(new String[]{}));
    }

    /**
     * Returns value associated with specified key wrapped with AnyValue object. It is assumed that this is of type
     * org.json.JSONObject.
     *
     * @param key for querying value from jsonobject
     * @return value associated with specified key
     */
    public AnyNode get(String key) {
        return new AnyNode(asJsonObject().get(key));
    }

    /**
     * Returns string representation of this. If it happens to have null, the value is treated as
     * org.json.JSONObject.NULL and "null" string is returned then.
     *
     * @return string representation of this
     */
    public String toString() {
        return this.obj.toString();
    }

    /**
     * Returns optional of object under specified key, wrapped with AnyNode object.
     * If underlying object is not of type org.json.JSONObject
     * or underlying object has no given key
     * or given key is null
     * then Optional.empty will be returned.
     *
     * @param key for querying value from AnyNode object
     * @return optional of object under specified key
     */
    public Option<AnyNode> getAsOption(String key) {
        try {
            AnyNode value = get(key);
            if (value.toString().equals("null")) {
                return Option.none();
            }
            return Option.some(value);
        } catch (JSONException ex) {
            return Option.none();
        }
    }

    /**
     * Converts underlying object to map representation with map values wrapped with AnyNode object. It is assumed that
     * underlying object is of type org.json.JSONObject.
     *
     * @return converts underlying object to map representation
     */
    public List<AnyNode> toList() {
        return List.ofAll(StreamSupport.stream(((JSONArray) this.obj).spliterator(), false).map(AnyNode::new));
    }

    /**
     * Checks if specified key is present in this. It is assumed that this is of type JSONObject.
     *
     * @param key is used to check presence in anynode object
     * @return true if specified key is present in this
     */
    public boolean has(String key) {
        return !getAsOption(key).isEmpty();
    }

    /**
     * Returns as JSONObject.
     *
     * @return jsonobject
     */
    private JSONObject asJsonObject() {
        return (JSONObject) this.obj;
    }


}

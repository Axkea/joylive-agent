/*
 * Copyright © ${year} ${owner} (${email})
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.live.agent.governance.policy;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.CRC32;

/**
 * The {@code PolicyId} class is an abstract implementation of the {@code IdGenerator} interface.
 * It represents a policy with an identifier that can be generated based on a URI. The class also
 * maintains a set of tags that can be used to supplement the policy with additional information.
 */
@Getter
public class PolicyId implements PolicyIdGen {

    /**
     * The key for the service name tag.
     */
    public static final String KEY_SERVICE_NAME = "service.name";

    /**
     * The key for the service group tag.
     */
    public static final String KEY_SERVICE_GROUP = "service.group";

    /**
     * The key for the service path tag.
     */
    public static final String KEY_SERVICE_PATH = "service.path";

    /**
     * The key for the service method tag.
     */
    public static final String KEY_SERVICE_METHOD = "service.method";

    /**
     * The key for the service variable tag.
     */
    public static final String KEY_SERVICE_VARIABLE = "service.variable";

    /**
     * The key for the service lane space ID tag.
     */
    public static final String KEY_SERVICE_LANE_SPACE_ID = "service.laneSpaceId";

    public static final String KEY_SERVICE_CONCURRENCY_LIMIT = "service.concurrencyLimit";

    public static final String KEY_SERVICE_RATE_LIMIT = "service.rateLimit";

    public static final String KEY_SERVICE_ROUTE = "service.route";

    public static final String KEY_SERVICE_CIRCUIT_BREAKER = "service.circuitBreaker";

    /**
     * The default group name for the service.
     */
    public static final String DEFAULT_GROUP = "default";

    /**
     * The unique identifier for the policy, generated based on the URI.
     */
    @Setter
    protected Long id;

    /**
     * The URI associated with the policy.
     */
    @Getter
    protected transient String uri;

    /**
     * A map of tags that provide additional context or metadata for the policy.
     */
    protected Map<String, String> tags;

    /**
     * Implements the {@code IdGenerator} interface to supplement the policy with a URI and tags.
     * If the URI is not set, it is updated with the value provided by the supplier. The unique
     * identifier for the policy is then generated based on the URI. The tags are also updated
     * with the provided map of tags.
     *
     * @param url  the supplier of the URI to be associated with the policy.
     * @param tags the map of tags to be associated with the policy.
     */
    @Override
    public void supplement(Supplier<String> url, Map<String, String> tags) {
        if (uri == null && url != null) {
            uri = url.get();
        }
        if (id == null && uri != null) {
            CRC32 crc32 = new CRC32();
            byte[] bytes = uri.getBytes(StandardCharsets.UTF_8);
            crc32.update(bytes, 0, bytes.length);
            id = Math.abs(crc32.getValue());
        }
        this.tags = tags;
    }

    /**
     * Returns a copy of the current tags, creating a new map if tags are null.
     *
     * @return an unmodifiable copy of the tags map.
     */
    protected Map<String, String> supplementTag() {
        return tags == null ? new HashMap<>() : new HashMap<>(tags);
    }

    /**
     * Supplements the existing tag map with additional key-value pairs.
     *
     * <p>This method takes a variable number of string arguments representing key-value pairs of tags to be added to the current tag map.
     * It creates a new map containing all the entries of the original tag map, if it exists,
     * and then adds the new key-value pairs to it.</p>
     *
     * <p>If the number of key-value pairs provided is not an even number, the last provided value will be ignored.</p>
     *
     * @param keyValues a variable number of string arguments where even indices represent keys and odd indices represent values.
     * @return a new map with the original tags supplemented by the new key-value pairs.
     */
    protected Map<String, String> supplementTag(String... keyValues) {
        Map<String, String> result = tags == null ? new HashMap<>() : new HashMap<>(tags);
        if (keyValues != null) {
            int pairs = keyValues.length / 2;
            for (int i = 0; i < pairs; i++) {
                result.put(keyValues[i * 2], keyValues[i * 2 + 1]);
            }
        }
        return result;
    }

    /**
     * Retrieves the value of the tag associated with the given key.
     *
     * @param key the key of the tag whose value is to be retrieved.
     * @return the value of the tag associated with the key, or null if the key is not present.
     */
    public String getTag(String key) {
        return tags == null ? null : tags.get(key);
    }

    /**
     * Generates a new id based on the given additional string.
     *
     * @param additional Aditional string to be used for generating the id.
     * @return id
     */
    public Long generateId(Supplier<String> additional) {
        String newUri = uri == null ? additional.get() : uri + additional.get();
        if (newUri != null) {
            CRC32 crc32 = new CRC32();
            byte[] bytes = newUri.getBytes(StandardCharsets.UTF_8);
            crc32.update(bytes, 0, bytes.length);
            return Math.abs(crc32.getValue());
        }
        return 0L;
    }
}


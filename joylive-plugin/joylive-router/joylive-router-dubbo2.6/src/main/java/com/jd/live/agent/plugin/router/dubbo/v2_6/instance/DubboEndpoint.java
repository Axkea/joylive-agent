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
package com.jd.live.agent.plugin.router.dubbo.v2_6.instance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.jd.live.agent.core.util.option.Converts;
import com.jd.live.agent.governance.instance.AbstractEndpoint;
import com.jd.live.agent.governance.instance.EndpointState;
import com.jd.live.agent.governance.request.ServiceRequest;

import static com.alibaba.dubbo.common.Constants.REMOTE_TIMESTAMP_KEY;

/**
 * Represents a network endpoint in a Dubbo RPC system, wrapping an {@link Invoker} instance.
 * <p>
 * This class holds information about a specific service endpoint, including its URL and the ability
 * to invoke the service. It provides methods to access basic network attributes such as the host,
 * port, and dynamic attributes like weight and state based on the service configuration and runtime
 * conditions.
 * </p>
 *
 * @param <T> The type of the service interface that this endpoint represents.
 */
public class DubboEndpoint<T> extends AbstractEndpoint {

    private final Invoker<T> invoker;

    private final URL url;

    public DubboEndpoint(Invoker<T> invoker) {
        this.invoker = invoker;
        this.url = invoker.getUrl();
    }

    public Invoker<T> getInvoker() {
        return invoker;
    }

    @Override
    public String getHost() {
        return url.getHost();
    }

    @Override
    public int getPort() {
        return url.getPort();
    }

    @Override
    public Long getTimestamp() {
        String timestamp = getLabel(REMOTE_TIMESTAMP_KEY);
        if (timestamp == null || timestamp.isEmpty()) {
            timestamp = getLabel(KEY_TIMESTAMP);
        }
        return Converts.getLong(timestamp, null);
    }

    @Override
    public Integer getOriginWeight(ServiceRequest request) {
        String weight = url.getMethodParameter(request.getMethod(), KEY_WEIGHT, null);
        if (weight == null || weight.isEmpty()) {
            weight = url.getParameter(KEY_WEIGHT);
        }
        return Converts.getInteger(weight, DEFAULT_WEIGHT);
    }

    @Override
    public String getLabel(String key) {
        return url.getParameter(key);
    }

    @Override
    public EndpointState getState() {
        return invoker.isAvailable() ? EndpointState.HEALTHY : EndpointState.DISABLE;
    }

    /**
     * Factory method to create a new {@code DubboEndpoint} instance for a given invoker.
     *
     * @param invoker The invoker for which the endpoint is to be created.
     * @return A new instance of {@code DubboEndpoint}.
     */
    public static DubboEndpoint<?> of(Invoker<?> invoker) {
        return new DubboEndpoint<>(invoker);
    }
}

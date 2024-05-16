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
package com.jd.live.agent.plugin.router.springcloud.v3.request;

import com.jd.live.agent.core.util.cache.LazyObject;
import com.jd.live.agent.governance.request.HttpMethod;
import com.jd.live.agent.governance.util.Cookies;
import feign.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an outbound request made using Feign, extending the capabilities of {@link AbstractClusterRequest}
 * to handle specifics of Feign requests such as options and cookie parsing.
 * <p>
 * This class encapsulates the details of a Feign request, including HTTP method, URI, headers, and cookies,
 * and provides utilities for parsing these elements from the Feign {@link Request}. It also integrates with
 * Spring's {@link LoadBalancerClientFactory} for load balancing capabilities.
 *
 * @since 1.0.0
 */
public class FeignClusterRequest extends AbstractClusterRequest<Request> {

    private static final String COOKIE_HEADER = "Cookie";

    private final Request.Options options;

    /**
     * Constructs a new {@code FeignOutboundRequest} with the specified Feign request, load balancer client factory,
     * and request options.
     *
     * @param request                   the Feign request
     * @param loadBalancerClientFactory the factory to create a load balancer client
     * @param options                   the options for the Feign request, such as timeouts
     */
    public FeignClusterRequest(Request request,
                               LoadBalancerClientFactory loadBalancerClientFactory,
                               Request.Options options) {
        super(request, loadBalancerClientFactory);
        this.options = options;
        this.uri = URI.create(request.url());
        this.queries = new LazyObject<>(() -> parseQuery(request.requestTemplate().queryLine()));
        this.headers = new LazyObject<>(() -> parseHeaders(request));
        this.cookies = new LazyObject<>(() -> Cookies.parse(request.headers().get(COOKIE_HEADER)));
    }


    @Override
    public HttpMethod getHttpMethod() {
        Request.HttpMethod method = request.httpMethod();
        try {
            return method == null ? null : HttpMethod.valueOf(method.name());
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    @Override
    protected RequestData buildRequestData() {
        HttpHeaders requestHeaders = new HttpHeaders();
        request.headers().forEach((key, value) -> requestHeaders.put(key, new ArrayList<>(value)));
        return new RequestData(org.springframework.http.HttpMethod.resolve(request.httpMethod().name()),
                getURI(), requestHeaders, null, new HashMap<>());
    }

    public Request.Options getOptions() {
        return options;
    }

    /**
     * Parses the headers from the Feign request into a map.
     *
     * @param request the Feign request
     * @return a map of header names to lists of header values
     */
    private Map<String, List<String>> parseHeaders(Request request) {
        return request.headers().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
    }
}

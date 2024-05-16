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
package com.jd.live.agent.plugin.router.springcloud.v3.cluster;

import com.jd.live.agent.core.util.Futures;
import com.jd.live.agent.core.util.type.ClassDesc;
import com.jd.live.agent.core.util.type.ClassUtils;
import com.jd.live.agent.core.util.type.FieldDesc;
import com.jd.live.agent.core.util.type.FieldList;
import com.jd.live.agent.governance.policy.service.cluster.RetryPolicy;
import com.jd.live.agent.governance.response.Response;
import com.jd.live.agent.plugin.router.springcloud.v3.instance.SpringEndpoint;
import com.jd.live.agent.plugin.router.springcloud.v3.request.BlockingClusterRequest;
import com.jd.live.agent.plugin.router.springcloud.v3.response.BlockingClusterResponse;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * The {@code BlockingCluster} class extends {@code AbstractClientCluster} to provide a blocking
 * mechanism for handling HTTP requests, integrating load balancing and retry logic. It utilizes
 * Spring Cloud's load balancing capabilities to distribute requests across service instances and
 * supports configurable retry mechanisms for handling transient failures.
 * <p>
 * This class is designed to work within a microservices architecture, leveraging Spring Cloud's
 * infrastructure components to facilitate resilient and scalable service-to-service communication.
 *
 * @see AbstractClientCluster
 */
public class BlockingCluster extends AbstractClientCluster<BlockingClusterRequest, BlockingClusterResponse> {

    private static final Set<String> RETRY_EXCEPTIONS = new HashSet<>(Arrays.asList(
            "java.io.IOException",
            "java.util.concurrent.TimeoutException",
            "org.springframework.cloud.client.loadbalancer.reactive.RetryableStatusCodeException"
    ));

    private static final String FIELD_LOAD_BALANCER_FACTORY = "loadBalancerFactory";

    private static final String FIELD_REQUEST_FACTORY = "requestFactory";

    /**
     * An interceptor for HTTP requests, used to apply additional processing or modification
     * to requests before they are executed.
     */
    private final ClientHttpRequestInterceptor interceptor;

    /**
     * A factory for creating load-balanced {@code LoadBalancerRequest} instances, supporting
     * the dynamic selection of service instances based on load.
     */
    private final LoadBalancerRequestFactory requestFactory;

    /**
     * A factory for creating {@code ReactiveLoadBalancer} instances for service discovery
     * and load balancing.
     */
    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

    /**
     * Constructs a {@code BlockingCluster} with the specified HTTP request interceptor.
     * Initializes the {@code requestFactory} and {@code loadBalancerFactory} fields by
     * reflectively accessing the interceptor's fields.
     *
     * @param interceptor the HTTP request interceptor to be used by this cluster
     */
    @SuppressWarnings("unchecked")
    public BlockingCluster(ClientHttpRequestInterceptor interceptor) {
        this.interceptor = interceptor;
        ClassDesc describe = ClassUtils.describe(interceptor.getClass());
        FieldList fieldList = describe.getFieldList();
        FieldDesc field = fieldList.getField(FIELD_REQUEST_FACTORY);
        this.requestFactory = (LoadBalancerRequestFactory) (field == null ? null : field.get(interceptor));
        field = fieldList.getField(FIELD_LOAD_BALANCER_FACTORY);
        this.loadBalancerFactory = (ReactiveLoadBalancer.Factory<ServiceInstance>) (field == null ? null : field.get(interceptor));
    }

    public ReactiveLoadBalancer.Factory<ServiceInstance> getLoadBalancerFactory() {
        return loadBalancerFactory;
    }

    @Override
    protected boolean isRetryable() {
        return interceptor instanceof RetryLoadBalancerInterceptor;
    }

    @Override
    public CompletionStage<BlockingClusterResponse> invoke(BlockingClusterRequest request, SpringEndpoint endpoint) {
        LoadBalancerRequest<ClientHttpResponse> lbRequest = requestFactory.createRequest(request.getRequest(), request.getBody(), request.getExecution());
        // TODO sticky session
        try {
            ClientHttpResponse response = lbRequest.apply(endpoint.getInstance());
            return CompletableFuture.completedFuture(new BlockingClusterResponse(response));
        } catch (Exception e) {
            return Futures.future(e);
        }
    }

    @Override
    public BlockingClusterResponse createResponse(Throwable throwable, BlockingClusterRequest request, SpringEndpoint endpoint) {
        return new BlockingClusterResponse(createException(throwable, request, endpoint));
    }

    @Override
    public boolean isRetryable(Response response) {
        // TODO modify isRetryable
        return RetryPolicy.isRetry(RETRY_EXCEPTIONS, response.getThrowable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(BlockingClusterResponse response, BlockingClusterRequest request, SpringEndpoint endpoint) {
        try {
            HttpHeaders responseHeaders = getHttpHeaders(response.getHeaders());
            RequestData requestData = request.getRequestData();
            HttpStatus httpStatus = response.getResponse().getStatusCode();
            request.lifecycles(l -> l.onComplete(new CompletionContext<>(
                    CompletionContext.Status.SUCCESS,
                    request.getLbRequest(),
                    endpoint.getResponse(),
                    request.getProperties().isUseRawStatusCodeInResponseData()
                            ? new ResponseData(responseHeaders, null, requestData, httpStatus.value())
                            : new ResponseData(httpStatus, responseHeaders, null, requestData))));
        } catch (IOException ignore) {
        }
    }
}

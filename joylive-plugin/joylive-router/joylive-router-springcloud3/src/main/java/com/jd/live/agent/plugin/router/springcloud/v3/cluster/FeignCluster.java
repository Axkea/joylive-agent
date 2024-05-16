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
import com.jd.live.agent.plugin.router.springcloud.v3.request.FeignClusterRequest;
import com.jd.live.agent.plugin.router.springcloud.v3.response.FeignClusterResponse;
import feign.Client;
import feign.Request;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.ResponseData;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class FeignCluster extends AbstractClientCluster<FeignClusterRequest, FeignClusterResponse> {

    private static final Set<String> RETRY_EXCEPTIONS = new HashSet<>(Arrays.asList(
            "java.io.IOException",
            "java.util.concurrent.TimeoutException",
            "org.springframework.cloud.client.loadbalancer.reactive.RetryableStatusCodeException"
    ));

    private static final String FIELD_DELEGATE = "delegate";

    private static final String FIELD_LOAD_BALANCER_CLIENT_FACTORY = "loadBalancerClientFactory";

    private final Client client;

    private final Client delegate;

    private final LoadBalancerClientFactory loadBalancerClientFactory;

    public FeignCluster(Client client) {
        this.client = client;
        ClassDesc describe = ClassUtils.describe(client.getClass());
        FieldList fieldList = describe.getFieldList();
        FieldDesc field = fieldList.getField(FIELD_DELEGATE);
        this.delegate = (Client) (field == null ? null : field.get(client));
        field = fieldList.getField(FIELD_LOAD_BALANCER_CLIENT_FACTORY);
        this.loadBalancerClientFactory = (LoadBalancerClientFactory) (field == null ? null : field.get(client));
    }

    public LoadBalancerClientFactory getLoadBalancerClientFactory() {
        return loadBalancerClientFactory;
    }

    @Override
    protected boolean isRetryable() {
        return client instanceof RetryableFeignBlockingLoadBalancerClient;
    }

    @Override
    public CompletionStage<FeignClusterResponse> invoke(FeignClusterRequest request, SpringEndpoint endpoint) {
        Request req = request.getRequest();
        String url = LoadBalancerUriTools.reconstructURI(endpoint.getInstance(), request.getURI()).toString();
        // TODO sticky session
        req = Request.create(req.httpMethod(), url, req.headers(), req.body(), req.charset(), req.requestTemplate());
        try {
            feign.Response response = delegate.execute(req, request.getOptions());
            return CompletableFuture.completedFuture(new FeignClusterResponse(response));
        } catch (IOException e) {
            return Futures.future(e);
        }
    }

    @Override
    public FeignClusterResponse createResponse(Throwable throwable, FeignClusterRequest request, SpringEndpoint endpoint) {
        return new FeignClusterResponse(createException(throwable, request, endpoint));
    }

    @Override
    public boolean isRetryable(Response response) {
        // TODO modify isRetryable
        return RetryPolicy.isRetry(RETRY_EXCEPTIONS, response.getThrowable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(FeignClusterResponse response, FeignClusterRequest request, SpringEndpoint endpoint) {
        HttpHeaders responseHeaders = getHttpHeaders(response.getHeaders());
        RequestData requestData = request.getRequestData();
        int status = response.getResponse().status();
        HttpStatus httpStatus = HttpStatus.resolve(response.getResponse().status());
        request.lifecycles(l -> l.onComplete(new CompletionContext<>(
                CompletionContext.Status.SUCCESS,
                request.getLbRequest(),
                endpoint.getResponse(),
                request.getProperties().isUseRawStatusCodeInResponseData()
                        ? new ResponseData(responseHeaders, null, requestData, status)
                        : new ResponseData(httpStatus, responseHeaders, null, requestData))));
    }
}

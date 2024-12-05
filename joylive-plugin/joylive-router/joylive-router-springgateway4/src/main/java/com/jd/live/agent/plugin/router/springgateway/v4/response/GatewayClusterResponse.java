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
package com.jd.live.agent.plugin.router.springgateway.v4.response;

import com.jd.live.agent.core.util.cache.UnsafeLazyObject;
import com.jd.live.agent.core.util.http.HttpUtils;
import com.jd.live.agent.governance.exception.ErrorPredicate;
import com.jd.live.agent.governance.exception.ServiceError;
import com.jd.live.agent.governance.response.AbstractHttpResponse.AbstractHttpOutboundResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.util.function.Supplier;

/**
 * GatewayClusterResponse
 *
 * @since 1.0.0
 */
public class GatewayClusterResponse extends AbstractHttpOutboundResponse<ServerHttpResponse> {

    private final UnsafeLazyObject<String> body;


    public GatewayClusterResponse(ServerHttpResponse response) {
        this(response, () -> null, null);
    }

    public GatewayClusterResponse(ServerHttpResponse response, Supplier<ServiceError> errorSupplier, Supplier<String> bodySupplier) {
        super(response, errorSupplier, null);
        this.headers = new UnsafeLazyObject<>(response::getHeaders);
        this.cookies = new UnsafeLazyObject<>(() -> HttpUtils.parseCookie(response.getCookies(), ResponseCookie::getValue));
        this.body = new UnsafeLazyObject<>(bodySupplier);
    }

    public GatewayClusterResponse(ServiceError error, ErrorPredicate retryPredicate) {
        super(error, retryPredicate);
        this.body = null;
    }

    @Override
    public String getCode() {
        Integer code = response == null ? null : response.getRawStatusCode();
        return code == null ? null : code.toString();
    }

    @Override
    public Object getResult() {
        return body == null ? null : body.get();
    }
}

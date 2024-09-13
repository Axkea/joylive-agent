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
package com.jd.live.agent.plugin.router.sofarpc.response;

import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.jd.live.agent.governance.response.AbstractRpcResponse.AbstractRpcOutboundResponse;
import com.jd.live.agent.governance.response.ServiceError;

import java.util.function.Predicate;

/**
 * Represents a response in the SOFA RPC framework.
 * <p>
 * The SOFA RPC framework is designed to provide a high-performance, scalable, and extensible RPC mechanism
 * suitable for microservices architecture. This interface is used to represent the responses that are
 * exchanged within the SOFA RPC framework, allowing for a standardized way of handling RPC responses.
 * </p>
 *
 * @since 1.0.0
 */
public interface SofaRpcResponse {

    /**
     * A concrete implementation of {@link SofaRpcResponse} that encapsulates the outbound response
     * of a SOFA RPC call. This class extends {@code AbstractRpcOutboundResponse<SofaResponse>} to
     * provide specific handling for SOFA RPC responses, including success and error states.
     */
    class SofaRpcOutboundResponse extends AbstractRpcOutboundResponse<SofaResponse> implements SofaRpcResponse {

        /**
         * Constructs a new {@code SofaRpcOutboundResponse} for a successful SOFA RPC call.
         *
         * @param response The {@link SofaResponse} object containing the data returned by the successful RPC call.
         */
        public SofaRpcOutboundResponse(SofaResponse response) {
            this(response, null);
        }

        /**
         * Constructs a new {@code SofaRpcOutboundResponse} for a successful SOFA RPC call.
         *
         * @param response  The {@link SofaResponse} object containing the data returned by the successful RPC call.
         * @param predicate An optional {@code Predicate<Response>} that can be used to evaluate
         *                  whether the call should be retried based on the response. Can be {@code null}
         *                  if retry logic is not applicable.
         */
        public SofaRpcOutboundResponse(SofaResponse response, Predicate<Throwable> predicate) {
            super(response, response != null && response.isError() ? new ServiceError(response.getErrorMsg(), true) : null, predicate);
        }

        /**
         * Constructs a new {@code SofaRpcOutboundResponse} for a failed SOFA RPC call.
         *
         * @param error The {@code Throwable} that represents the error occurred during the RPC call.
         * @param predicate An optional {@code Predicate<Response>} that can be used to evaluate
         *                  whether the call should be retried based on the response. Can be {@code null}
         *                  if retry logic is not applicable.
         */
        public SofaRpcOutboundResponse(ServiceError error, Predicate<Throwable> predicate) {
            super(null, error, predicate);
        }

    }
}


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
package com.jd.live.agent.plugin.router.springweb.v6.definition;

import com.jd.live.agent.core.bytekit.matcher.MatcherBuilder;
import com.jd.live.agent.core.extension.annotation.*;
import com.jd.live.agent.core.inject.annotation.Inject;
import com.jd.live.agent.core.inject.annotation.Injectable;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinition;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinitionAdapter;
import com.jd.live.agent.core.plugin.definition.PluginDefinitionAdapter;
import com.jd.live.agent.governance.config.GovernanceConfig;
import com.jd.live.agent.governance.invoke.InvocationContext;
import com.jd.live.agent.plugin.router.springweb.v6.interceptor.DispatcherHandlerInterceptor;
import com.jd.live.agent.plugin.router.springweb.v6.interceptor.HandleResultInterceptor;

/**
 * DispatcherHandlerDefinition
 *
 * @author Zhiguo.Chen
 * @since 1.0.0
 */
@Injectable
@Extension(value = "DispatcherHandlerDefinition_v6")
@ConditionalOnProperties(value = {
        @ConditionalOnProperty(name = {
                GovernanceConfig.CONFIG_LIVE_ENABLED,
                GovernanceConfig.CONFIG_LANE_ENABLED,
                GovernanceConfig.CONFIG_FLOW_CONTROL_ENABLED
        }, matchIfMissing = true, relation = ConditionalRelation.OR),
        @ConditionalOnProperty(value = GovernanceConfig.CONFIG_LIVE_SPRING_ENABLED, matchIfMissing = true)
}, relation = ConditionalRelation.AND)
@ConditionalOnClass(DispatcherHandlerDefinition.TYPE_DISPATCHER_HANDLER)
@ConditionalOnClass(DispatcherHandlerDefinition.TYPE_ERROR_RESPONSE)
@ConditionalOnClass(DispatcherHandlerDefinition.REACTOR_MONO)
public class DispatcherHandlerDefinition extends PluginDefinitionAdapter {

    protected static final String TYPE_DISPATCHER_HANDLER = "org.springframework.web.reactive.DispatcherHandler";

    protected static final String TYPE_ERROR_RESPONSE = "org.springframework.web.ErrorResponse";

    // For spring web 6
    private static final String METHOD_HANDLE_REQUEST_WITH = "handleRequestWith";

    private static final String METHOD_HANDLE_RESULT = "handleResult";

    private static final String METHOD_DO_HANDLE_RESULT = "doHandleResult";

    private static final String[] ARGUMENT_HANDLE = new String[]{
            "org.springframework.web.server.ServerWebExchange",
            "java.lang.Object"
    };

    private static final String[] ARGUMENT_HANDLE_RESULT = new String[]{
            "org.springframework.web.server.ServerWebExchange",
            "org.springframework.web.reactive.HandlerResult",
            "java.lang.String"
    };

    protected static final String REACTOR_MONO = "reactor.core.publisher.Mono";

    @Inject(InvocationContext.COMPONENT_INVOCATION_CONTEXT)
    private InvocationContext context;

    public DispatcherHandlerDefinition() {
        this.matcher = () -> MatcherBuilder.named(TYPE_DISPATCHER_HANDLER);
        this.interceptors = new InterceptorDefinition[]{
                new InterceptorDefinitionAdapter(
                        MatcherBuilder.named(METHOD_HANDLE_REQUEST_WITH).
                                and(MatcherBuilder.arguments(ARGUMENT_HANDLE)),
                        () -> new DispatcherHandlerInterceptor(context)
                ),
                new InterceptorDefinitionAdapter(
                        MatcherBuilder.named(METHOD_HANDLE_RESULT).
                                and(MatcherBuilder.arguments(ARGUMENT_HANDLE_RESULT)),
                        HandleResultInterceptor::new),
                new InterceptorDefinitionAdapter(
                        MatcherBuilder.named(METHOD_DO_HANDLE_RESULT).
                                and(MatcherBuilder.arguments(ARGUMENT_HANDLE_RESULT)),
                        HandleResultInterceptor::new)
        };
    }
}

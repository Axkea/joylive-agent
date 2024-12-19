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
package com.jd.live.agent.plugin.router.springcloud.v2.definition;

import com.jd.live.agent.core.bytekit.matcher.MatcherBuilder;
import com.jd.live.agent.core.extension.annotation.ConditionalOnClass;
import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.core.inject.annotation.Inject;
import com.jd.live.agent.core.inject.annotation.Injectable;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinition;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinitionAdapter;
import com.jd.live.agent.core.plugin.definition.PluginDefinitionAdapter;
import com.jd.live.agent.governance.invoke.InvocationContext;
import com.jd.live.agent.plugin.router.springcloud.v2.condition.ConditionalOnSpringCloud2GovernanceEnabled;
import com.jd.live.agent.plugin.router.springcloud.v2.interceptor.FeignClusterInterceptor;

/**
 * FeignRibbonClusterDefinition
 *
 * @since 1.5.0
 */
@Injectable
@Extension(value = "FeignRibbonClusterDefinition_v2")
@ConditionalOnSpringCloud2GovernanceEnabled
@ConditionalOnClass(FeignRibbonClusterDefinition.TYPE_FEIGN_BLOCKING_LOADBALANCER_CLIENT)
public class FeignRibbonClusterDefinition extends PluginDefinitionAdapter {

    protected static final String TYPE_FEIGN_BLOCKING_LOADBALANCER_CLIENT = "org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient";

    private static final String METHOD_EXECUTE = "execute";

    @Inject(InvocationContext.COMPONENT_INVOCATION_CONTEXT)
    private InvocationContext context;

    public FeignRibbonClusterDefinition() {
        this.matcher = () -> MatcherBuilder.named(TYPE_FEIGN_BLOCKING_LOADBALANCER_CLIENT);
        this.interceptors = new InterceptorDefinition[]{
                new InterceptorDefinitionAdapter(MatcherBuilder.named(METHOD_EXECUTE), () -> new FeignClusterInterceptor(context))
        };
    }
}

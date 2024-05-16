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
package com.jd.live.agent.plugin.transmission.springcloud.v3.definition;

import com.jd.live.agent.core.bytekit.matcher.MatcherBuilder;
import com.jd.live.agent.core.extension.annotation.*;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinitionAdapter;
import com.jd.live.agent.core.plugin.definition.PluginDefinition;
import com.jd.live.agent.core.plugin.definition.PluginDefinitionAdapter;
import com.jd.live.agent.governance.config.GovernanceConfig;
import com.jd.live.agent.plugin.transmission.springcloud.v3.interceptor.DefaultExchangeFunctionInterceptor;

/**
 * DefaultExchangeFunctionDefinition
 *
 * @since 1.0.0
 */
@Extension(value = "DefaultExchangeFunctionDefinition_v3", order = PluginDefinition.ORDER_TRANSMISSION)
@ConditionalOnProperties(value = {
        @ConditionalOnProperty(value = GovernanceConfig.CONFIG_LIVE_ENABLED, matchIfMissing = true),
        @ConditionalOnProperty(value = GovernanceConfig.CONFIG_LANE_ENABLED, matchIfMissing = true)
}, relation = ConditionalRelation.OR)
@ConditionalOnClass(DefaultExchangeFunctionDefinition.TYPE_DEFAULT_EXCHANGE_FUNCTION)
public class DefaultExchangeFunctionDefinition extends PluginDefinitionAdapter {

    protected static final String TYPE_DEFAULT_EXCHANGE_FUNCTION = "org.springframework.web.reactive.function.client.ExchangeFunctions.DefaultExchangeFunction";

    private static final String METHOD_EXCHANGE = "exchange";

    private static final String[] ARGUMENT_EXCHANGE = new String[]{
            "org.springframework.web.reactive.function.client.ClientRequest"
    };

    public DefaultExchangeFunctionDefinition() {
        super(MatcherBuilder.isImplement(TYPE_DEFAULT_EXCHANGE_FUNCTION),
                new InterceptorDefinitionAdapter(
                        MatcherBuilder.named(METHOD_EXCHANGE).
                                and(MatcherBuilder.arguments(ARGUMENT_EXCHANGE)),
                        new DefaultExchangeFunctionInterceptor()));
    }
}

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
package com.jd.live.agent.plugin.router.springcloud.v3.definition;

import com.jd.live.agent.core.bytekit.matcher.MatcherBuilder;
import com.jd.live.agent.core.extension.annotation.ConditionalOnClass;
import com.jd.live.agent.core.extension.annotation.ConditionalOnMissingClass;
import com.jd.live.agent.core.extension.annotation.ConditionalOnProperty;
import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinitionAdapter;
import com.jd.live.agent.core.plugin.definition.PluginDefinitionAdapter;
import com.jd.live.agent.governance.config.GovernanceConfig;
import com.jd.live.agent.plugin.router.springcloud.v3.interceptor.ZonePreferenceInterceptor;

/**
 * ZonePreferenceDefinition
 *
 * @since 1.0.0
 */
@Extension(value = "ZonePreferenceDefinition_v3")
@ConditionalOnProperty(name = GovernanceConfig.CONFIG_LIVE_ENABLED, matchIfMissing = true)
@ConditionalOnProperty(name = GovernanceConfig.CONFIG_LIVE_SPRING_ENABLED, matchIfMissing = true)
@ConditionalOnClass(ZonePreferenceDefinition.TYPE_ZONE_PREFERENCE_SERVICE_INSTANCE_LIST_SUPPLIER)
@ConditionalOnClass(BlockingClusterDefinition.TYPE_LOAD_BALANCER_PROPERTIES)
@ConditionalOnMissingClass(BlockingClusterDefinition.TYPE_HTTP_STATUS_CODE)
public class ZonePreferenceDefinition extends PluginDefinitionAdapter {

    protected static final String TYPE_ZONE_PREFERENCE_SERVICE_INSTANCE_LIST_SUPPLIER = "org.springframework.cloud.loadbalancer.core.ZonePreferenceServiceInstanceListSupplier";

    private static final String METHOD_FILTERED_BY_ZONE = "filteredByZone";

    private static final String[] ARGUMENTS_FILTERED_BY_ZONE = new String[]{
            "java.util.List"
    };

    public ZonePreferenceDefinition() {
        super(TYPE_ZONE_PREFERENCE_SERVICE_INSTANCE_LIST_SUPPLIER,
                new InterceptorDefinitionAdapter(
                        MatcherBuilder.named(METHOD_FILTERED_BY_ZONE).
                                and(MatcherBuilder.arguments(ARGUMENTS_FILTERED_BY_ZONE)),
                        new ZonePreferenceInterceptor()
                ));
    }
}

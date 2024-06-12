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
package com.jd.live.agent.plugin.registry.sofarpc.interceptor;

import com.alipay.sofa.rpc.bootstrap.ProviderBootstrap;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.jd.live.agent.bootstrap.bytekit.context.ExecutableContext;
import com.jd.live.agent.core.instance.Application;
import com.jd.live.agent.core.plugin.definition.InterceptorAdaptor;
import com.jd.live.agent.governance.policy.PolicySupplier;

/**
 * ProviderBootstrapInterceptor
 */
public class ProviderBootstrapInterceptor extends InterceptorAdaptor {

    private final Application application;

    private final PolicySupplier policySupplier;

    public ProviderBootstrapInterceptor(Application application, PolicySupplier policySupplier) {
        this.application = application;
        this.policySupplier = policySupplier;
    }

    @Override
    public void onEnter(ExecutableContext ctx) {
        ProviderConfig<?> config = ((ProviderBootstrap<?>) ctx.getTarget()).getProviderConfig();
        application.label((key, value) -> {
            String old = config.getParameter(key);
            if (old == null || old.isEmpty()) {
                config.setParameter(key, value);
            }
        });
        policySupplier.subscribe(config.getInterfaceId());
    }
}

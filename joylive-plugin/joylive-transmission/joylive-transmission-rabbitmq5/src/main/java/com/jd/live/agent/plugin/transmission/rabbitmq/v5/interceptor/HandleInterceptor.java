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
package com.jd.live.agent.plugin.transmission.rabbitmq.v5.interceptor;

import com.jd.live.agent.bootstrap.bytekit.context.ExecutableContext;
import com.jd.live.agent.governance.context.bag.CargoRequire;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Envelope;

import java.util.List;

public class HandleInterceptor extends AbstractConsumerInterceptor {

    public HandleInterceptor(List<CargoRequire> requires) {
        super(requires);
    }

    @Override
    public void onEnter(ExecutableContext ctx) {
        Envelope envelope = ctx.getArgument(1);
        BasicProperties props = ctx.getArgument(2);
        restore(props, envelope);
    }

}

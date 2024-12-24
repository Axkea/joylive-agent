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
package com.jd.live.agent.governance.annotation;

import com.jd.live.agent.core.extension.annotation.ConditionalComposite;
import com.jd.live.agent.core.extension.annotation.ConditionalOnProperty;
import com.jd.live.agent.governance.config.GovernanceConfig;

import java.lang.annotation.*;

/**
 * An annotation used to mark a type as requiring either the protect feature to be enabled, and the flow control
 * feature to be disabled.
 * <p>
 * This annotation is used to indicate that a type requires either the protect feature to be enabled, and the flow
 * control feature to be disabled. The presence of this annotation on a type will trigger specific behavior in the
 * governance processing logic.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(value = GovernanceConfig.CONFIG_LIVE_ENABLED)
@ConditionalOnProperty(value = GovernanceConfig.CONFIG_PROTECT_ENABLED)
@ConditionalComposite
public @interface ConditionalOnProtectEnabled {

}
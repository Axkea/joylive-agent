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
package com.jd.live.agent.governance.policy.service.failover;

import com.jd.live.agent.governance.policy.PolicyInherit.PolicyInheritWithId;
import com.jd.live.agent.governance.policy.service.annotation.Consumer;
import lombok.Getter;
import lombok.Setter;

/**
 * FailoverPolicy
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Consumer
public class FailoverPolicy implements PolicyInheritWithId<FailoverPolicy> {

    private Long id;

    private Integer retry;

    private Integer timeoutInMilliseconds;

    @Override
    public void supplement(FailoverPolicy source) {
        if (source == null) {
            return;
        }
        if (retry == null) {
            retry = source.retry;
        }
        if (timeoutInMilliseconds == null) {
            timeoutInMilliseconds = source.timeoutInMilliseconds;
        }
    }
}

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
package com.jd.live.agent.plugin.router.gprc.loadbalance;

import com.jd.live.agent.bootstrap.logger.Logger;
import com.jd.live.agent.bootstrap.logger.LoggerFactory;
import com.jd.live.agent.plugin.router.gprc.exception.GrpcStatus;
import io.grpc.LoadBalancer.PickResult;
import io.grpc.LoadBalancer.PickSubchannelArgs;
import io.grpc.LoadBalancer.SubchannelPicker;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class that extends the SubchannelPicker class to provide a live subchannel picking strategy.
 */
public class LiveSubchannelPicker extends SubchannelPicker {

    private static final Logger logger = LoggerFactory.getLogger(LiveSubchannelPicker.class);

    private final PickResult pickResult;

    private final List<LiveSubchannel> subchannels;

    private final AtomicLong counter = new AtomicLong();

    public LiveSubchannelPicker(PickResult pickResult) {
        this(pickResult, null);
    }

    public LiveSubchannelPicker(List<LiveSubchannel> subchannels) {
        this(null, subchannels);
    }

    public LiveSubchannelPicker(PickResult pickResult, List<LiveSubchannel> subchannels) {
        this.pickResult = pickResult;
        this.subchannels = subchannels;
    }

    @Override
    public PickResult pickSubchannel(PickSubchannelArgs args) {
        if (pickResult != null) {
            return pickResult;
        } else {
            LivePickerAdvice advice = args.getCallOptions().getOption(LivePickerAdvice.KEY_PICKER_ADVICE);
            LiveSubchannel subchannel = null;
            if (advice != null) {
                try {
                    subchannel = advice.elect(subchannels);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    return PickResult.withError(GrpcStatus.createException(e));
                }
            }
            if (subchannel != null) {
                return PickResult.withSubchannel(subchannel.getSubchannel());
            } else {
                long v = counter.getAndIncrement();
                if (v < 0) {
                    counter.set(0);
                    v = counter.getAndIncrement();
                }
                int index = (int) (v % subchannels.size());
                subchannel = subchannels.get(index);
                return PickResult.withSubchannel(subchannel.getSubchannel());
            }
        }
    }
}

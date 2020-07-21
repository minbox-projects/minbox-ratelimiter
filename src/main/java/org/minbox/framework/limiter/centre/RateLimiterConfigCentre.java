/*
 * Copyright [2019] [恒宇少年 - 于起宇]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package org.minbox.framework.limiter.centre;


import org.minbox.framework.limiter.exception.RateLimiterException;

/**
 * ApiBoot RateLimiter Distributed Configuration Center
 *
 * @author 恒宇少年
 */
public interface RateLimiterConfigCentre {
    /**
     * Getting QPS from Configuration Center
     *
     * @param configKey config key
     * @return QPS value
     * @throws RateLimiterException RateLimiter Exception
     */
    Long getQps(String configKey) throws RateLimiterException;

    /**
     * QPS Setting to Configuration Center
     *
     * @param configKey config key
     * @param QPS       QPS value
     * @throws RateLimiterException ApiBoot Exception
     */
    void setQps(String configKey, Long QPS) throws RateLimiterException;
}

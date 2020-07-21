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

package org.minbox.framework.limiter.centre.support;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.listener.impl.PropertiesListener;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.minbox.framework.limiter.centre.RateLimiterConfigCentre;
import org.minbox.framework.limiter.exception.RateLimiterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * Nacos Config Support
 * The {@link RateLimiterConfigCentre} Nacos implement class
 *
 * @author 恒宇少年
 */
public class NacosRateLimiterConfigCentre extends AbstractRateLimiterConfigCentre {
    /**
     * logger instance
     */
    static Logger logger = LoggerFactory.getLogger(NacosRateLimiterConfigCentre.class);
    /**
     * Nacos Config Service
     */
    private ConfigService configService;
    /**
     * rate limiter config properties
     */
    private Properties configProperties;

    public NacosRateLimiterConfigCentre(ConfigService configService) {
        this.configService = configService;

        // check configService not null
        Assert.notNull(configService, "ConfigService is required.");

        // load config data from nacos
        String configData = loadConfigData();

        // convert config data to properties
        this.configProperties = toProperties(configData);
        logger.info("ApiBoot RateLimiter nacos config properties load complete.");

        // Enable monitoring of receiving configuration changes
        addConfigChangeListener();
    }

    /**
     * get QPS config from nacos
     * for example properties:
     * 1. /user/list = .user.list: 5
     * 2. /user/detail = .user.detail: 10
     *
     * @param configKey config key
     * @return qps
     * @throws RateLimiterException ApiBoot Exception
     */
    @Override
    public Long getQps(String configKey) throws RateLimiterException {
        try {
            String propertyKey = formatPropertyKey(configKey);
            String qps = this.configProperties.getProperty(propertyKey);
            if (!StringUtils.isEmpty(qps)) {
                return Long.valueOf(qps);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return DEFAULT_QPS;
    }

    /**
     * set qps config to nacos
     *
     * @param configKey config key
     * @param QPS       QPS value
     * @throws RateLimiterException ApiBoot Exception
     */
    @Override
    public void setQps(String configKey, Long QPS) throws RateLimiterException {
        try {
            String propertyKey = formatPropertyKey(configKey);
            // update local cache config
            this.configProperties.setProperty(propertyKey, String.valueOf(QPS));
            // convert properties to string
            String configData = fromProperties(this.configProperties);
            if (!StringUtils.isEmpty(configData)) {
                // execute publish config
                this.configService.publishConfig(DATA_ID, Constants.DEFAULT_GROUP, configData);
                logger.info("ApiBoot RateLimiter nacos config publish successfully.");
            }
        } catch (Exception e) {
            throw new RateLimiterException(e.getMessage(), e);
        }
    }

    /**
     * load rate limiter config data
     *
     * @return config data
     * @throws RateLimiterException ApiBoot Exception
     */
    protected String loadConfigData() throws RateLimiterException {
        try {
            return configService.getConfig(DATA_ID, Constants.DEFAULT_GROUP, NacosUtils.DEFAULT_TIMEOUT);
        } catch (NacosException e) {
            logger.error(e.getMessage(), e);
        }
        throw new RateLimiterException("Load ApiBoot RateLimiter config data fail.");
    }

    /**
     * add config change listener
     * If you modify the configuration through the nacos configuration center,
     * listen to the changes and update the content Properties.
     */
    protected void addConfigChangeListener() throws RateLimiterException {
        try {
            configService.addListener(DATA_ID, Constants.DEFAULT_GROUP, new PropertiesListener() {
                /**
                 * Update local properties
                 * @param properties Changed configuration properties
                 */
                @Override
                public void innerReceive(Properties properties) {
                    configProperties = properties;
                    logger.info("Update local current RateLimiter configuration is complete，content：{}", properties.toString());
                }
            });
        } catch (Exception e) {
            logger.error("Configuration change listener failed to open.", e);
        }
    }
}

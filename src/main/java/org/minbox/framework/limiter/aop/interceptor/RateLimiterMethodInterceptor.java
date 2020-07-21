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

package org.minbox.framework.limiter.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.minbox.framework.limiter.MinBoxRateLimiter;
import org.minbox.framework.limiter.result.RateLimiterOverFlowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;

/**
 * ApiBoot RateLimiter MethodInterceptor
 * Current Limitation Request
 *
 * @author 恒宇少年
 */
public class RateLimiterMethodInterceptor implements MethodInterceptor {
    /**
     * logger instance
     */
    static Logger logger = LoggerFactory.getLogger(RateLimiterMethodInterceptor.class);
    /**
     * ApiBoot RateLimiter
     */
    private MinBoxRateLimiter minBoxRateLimiter;
    /**
     * Response results after flow exceeding
     */
    private RateLimiterOverFlowResponse overFlowRequest;

    public RateLimiterMethodInterceptor(MinBoxRateLimiter minBoxRateLimiter, RateLimiterOverFlowResponse overFlowRequest) {
        this.minBoxRateLimiter = minBoxRateLimiter;
        this.overFlowRequest = overFlowRequest;
        Assert.notNull(minBoxRateLimiter, "No ApiBootRateLimiter implementation class instance.");
        logger.info("ApiBootDefaultRateLimiterInterceptorHandler load complete.");
    }

    /**
     * Processing Current Limited Business Logic
     *
     * @param invocation method invocation
     * @return method result
     * @throws Throwable error instance
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
            Method executeMethod = invocation.getMethod();

            org.minbox.framework.limiter.annotation.RateLimiter rateLimiter = this.getMethodAnnotation(targetClass, executeMethod, org.minbox.framework.limiter.annotation.RateLimiter.class);

            // request key
            String requestKey = formatRequestKey(targetClass, executeMethod);
            logger.debug("RateLimiter Request Key：{}", requestKey);
            boolean acquire = this.minBoxRateLimiter.tryAcquire(rateLimiter.QPS(), requestKey);
            if (acquire) {
                return invocation.proceed();
            }
        } catch (Exception e) {
            logger.error("Current Limiting Request Encountered Exception.", e);
            throw e;
        }
        // If an instance is created
        if (!ObjectUtils.isEmpty(overFlowRequest)) {
            return overFlowRequest.overflow(invocation.getArguments());
        }
        return null;
    }

    /**
     * format request key
     *
     * @param targetClass   target class
     * @param executeMethod execute method
     * @return request key
     */
    private String formatRequestKey(Class<?> targetClass, Method executeMethod) {
        return String.format("%s#%s", targetClass.getName(), executeMethod.getName());
    }

    /**
     * get method declared annotation
     *
     * @param targetClass     class instance
     * @param method          method instance
     * @param annotationClass annotation class
     * @param <T>             annotation type
     * @return annotation instance
     */
    private <T> T getMethodAnnotation(Class targetClass, Method method, Class annotationClass) {
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // declared method object instance
        Method declaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        return (T) declaredMethod.getDeclaredAnnotation(annotationClass);
    }
}

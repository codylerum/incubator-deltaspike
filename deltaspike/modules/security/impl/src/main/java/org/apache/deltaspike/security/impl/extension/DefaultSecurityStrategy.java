/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.security.impl.extension;

import org.apache.deltaspike.security.spi.authorization.SecurityStrategy;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * {@inheritDoc}
 */
@Dependent
public class DefaultSecurityStrategy implements SecurityStrategy
{
    private static final long serialVersionUID = 7992336651801599079L;

    @Inject
    private BeanManager beanManager;

    @Inject
    private SecurityExtension securityExtension;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(InvocationContext invocationContext) throws Exception
    {
        Method method = invocationContext.getMethod();

        SecurityMetaDataStorage metaDataStorage = securityExtension.getMetaDataStorage();

        for (Authorizer authorizer : metaDataStorage.getAuthorizers(invocationContext.getTarget().getClass(), method))
        {
            if (authorizer.isBeforeMethodInvocationAuthorizer())
            {
                authorizer.authorize(invocationContext, null, this.beanManager);
            }
        }

        Object result = invocationContext.proceed();

        for (Authorizer authorizer : metaDataStorage.getAuthorizers(invocationContext.getTarget().getClass(), method))
        {
            if (authorizer.isAfterMethodInvocationAuthorizer())
            {
                authorizer.authorize(invocationContext, result, this.beanManager);
            }
        }
        
        return result;
    }
}

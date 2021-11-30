/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.ejb.containers.interceptors;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;

import com.sun.ejb.containers.InvocationHandlerUtil;


import com.sun.enterprise.container.common.spi.InterceptorInvoker;

/**
 *
 * @author Kenneth Saks
 */

public final class InterceptorInvocationHandler
    implements InvocationHandler, InterceptorInvoker {

    // The actual instance of the application class
    private Object targetInstance;

    // The object held by the application
    private Object clientProxy;

    private Object[] interceptorInstances;
    private InterceptorManager interceptorManager;

    private static Object[] emptyArray = new Object[] {};


    public void init(Object targetInstance, Object[] interceptorInstances,
                     Object clientProxy, InterceptorManager manager)
                     {
        this.targetInstance = targetInstance;
        this.interceptorInstances = interceptorInstances;
        this.clientProxy = clientProxy;
        interceptorManager = manager;

    }

    public Object getProxy() {
        return clientProxy;
    }

    public Object getTargetInstance() {
        return targetInstance;
    }

    public Object[] getInterceptorInstances() {
        return interceptorInstances;
    }

    public void invokeAroundConstruct() throws Exception {

         invokeCallback(CallbackType.AROUND_CONSTRUCT);
         targetInstance = interceptorManager.getTargetInstance();

    }

    public void invokePostConstruct() throws Exception {

         invokeCallback(CallbackType.POST_CONSTRUCT);

    }

    public void invokePreDestroy() throws Exception {

         invokeCallback(CallbackType.PRE_DESTROY);
    }

    private void invokeCallback(CallbackType type) throws Exception {

        try {
            interceptorManager.intercept(type, targetInstance,
                    interceptorInstances);
        } catch(Exception e) {
            throw e;
        } catch(Throwable t) {
            throw new Exception(t);
        }

    }


    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {


        Class methodClass = method.getDeclaringClass();
        if( methodClass == java.lang.Object.class )  {
            return InvocationHandlerUtil.
                invokeJavaObjectMethod(this, method, args);
        }

        Object returnValue = null;

        try {


            Method beanClassMethod = targetInstance.getClass().getMethod
                (method.getName(), method.getParameterTypes());

            InterceptorManager.InterceptorChain chain =
                    interceptorManager.getAroundInvokeChain(null, beanClassMethod);

            Object[] theArgs = (args == null) ? emptyArray : args;

            // Create context for around invoke invocation.  Make sure method set on
            // InvocationContext is from bean class.
            AroundInvokeInvocationContext invContext =
                    new AroundInvokeInvocationContext(targetInstance, interceptorInstances, chain,
                            beanClassMethod, theArgs );

            returnValue = interceptorManager.intercept(chain, invContext);

        } catch(NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        } catch(InvocationTargetException ite) {
            throw ite.getCause();
        }

        return returnValue;

    }

    @Override
    public String toString() {
        return (targetInstance != null)? targetInstance.toString() : super.toString();
    }

}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.*;

import java.util.ArrayList;
import java.util.List;

/**
 * KiWi (LMF) Content Util
 * 
 * @author Sebastian Schaffert
 */
public class CDIContext {
    private static BeanManager beanManager;

    private static final List<String> beanManagerLocations;

    private static Logger log = LoggerFactory.getLogger(CDIContext.class);

    static {
        beanManagerLocations = new ArrayList<String>();
        beanManagerLocations.add("java:comp/env/BeanManager");
        beanManagerLocations.add("java:comp/BeanManager");
        beanManagerLocations.add("java:app/BeanManager");
    }


    protected static BeanManager getBeanManager() {
        if (beanManager == null)
        {
            beanManager = lookupBeanManager();
        }
        return beanManager;
    }


    private static BeanManager lookupBeanManager() {
        for (String location : beanManagerLocations) {
            try {
                return (BeanManager) new InitialContext().lookup(location);
            } catch (NameNotFoundException e) {
                // do nothing
            } catch (NamingException e) {
                log.error(
                        "naming exception for path {}; this probably means that JNDI is not set up properly (see e.g. http://code.google.com/p/lmf/wiki/InstallationSetup#Specific_Settings_for_Tomcat )",
                        location, e);
            }
        }
        // in case no JNDI resource for the bean manager is found, display the JNDI context for debugging
        try {
            log.info("Could not find BeanManager in {}; list of JNDI context follows", beanManagerLocations);
            showJndiContext(new InitialContext(),"java:", "");
        } catch (NamingException e) {
            log.error("error listing JNDI context",e);
        }
        throw new IllegalArgumentException("Could not find BeanManager in " + beanManagerLocations);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> type) {

        BeanManager beanManager = getBeanManager();
        Bean<T> bean = (Bean<T>) beanManager.getBeans(type).iterator().next();
        CreationalContext<T> context = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, type, context);
    }

    /**
     * Return all injectable instances of the given type.
     * @param type
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getInstances(Class<T> type) {

        BeanManager beanManager = getBeanManager();

        List<T> result = new ArrayList<T>();
        for(Bean<?> bean :  beanManager.getBeans(type)) {
            CreationalContext<T> context = beanManager.createCreationalContext((Bean<T>)bean);
            result.add((T) beanManager.getReference(bean, type, context));
        }

        return result;
    }


    public static void showJndiContext(Context ctx, String name, String path) {
        try {
            NamingEnumeration<NameClassPair> bindings = ctx.list(name);
            while(bindings.hasMoreElements()) {
                NameClassPair pair = bindings.nextElement();
                log.info("Found JNDI resource: {}{} = {}", path,pair.getName(),pair.getClassName());
                if(pair.getClassName().endsWith("NamingContext")) {
                    showJndiContext((Context)ctx.lookup(name+ (name.length()>5?"/":"")+pair.getName()),"",path+"--");
                }
            }
        }catch ( NamingException ex ) {}
    }


}

/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.util;


/**
 * <p>This code is borrowed directly from OSCore, but is duplicated
 * here to avoid having to add a dependency on the entire OSCore jar.</p>
 *
 * <p>If much more code from OSCore is needed then it might be wiser to
 * bite the bullet and add a dependency.</p>
 */
public class ClassLoaderUtil {
    
    private ClassLoaderUtil() {
    }
    
    /**
     * Load a class with a given name.
     *
     * It will try to load the class in the following order:
     * <ul>
     *  <li>From Thread.currentThread().getContextClassLoader()
     *  <li>Using the basic Class.forName()
     *  <li>From ClassLoaderUtil.class.getClassLoader()
     *  <li>From the callingClass.getClassLoader()
     * </ul>
     *
     * @param className The name of the class to load
     * @param callingClass The Class object of the calling object
     * @throws ClassNotFoundException If the class cannot be found anywhere.
     */
    public static Class loadClass(String className, Class callingClass) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                try {
                    return ClassLoaderUtil.class.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException exc) {
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
        }
    }
}

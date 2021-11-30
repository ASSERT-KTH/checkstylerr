/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.flashlight;

import com.sun.enterprise.config.serverbeans.MonitoringService;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.glassfish.api.monitoring.DTraceContract;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.flashlight.impl.core.FlashlightProbeProvider;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Note that you MUST call an initilizer for this class for DTrace!
 * Why? We do not want the overhead of being a Service that implements a Contract
 * which is what you need to get a habitat object!
 *
 * // TODO -- just make this a Service and inject the habitat and simplify the code a bit!
 * @author Byron Nevins
 */
public class FlashlightUtils {
    private static final Logger logger = FlashlightLoggerInfo.getLogger();

    private FlashlightUtils() {
        // All static.  No instances allowed.
    }

    public static void initialize(ServiceLocator h, MonitoringService mc) {
        // only do once -- ignore multiple calls...
        synchronized (LOCK) {
            if (habitat == null) {
                habitat = h;
                monConfig = mc;
                setDTraceEnabled(Boolean.parseBoolean(monConfig.getDtraceEnabled()));
                setMonitoringEnabled(Boolean.parseBoolean(monConfig.getMonitoringEnabled()));

                // order mattrs.  the next method is depending on the previous
                //methods having been run already...
                setDTraceAvailabilty();
            }
        }
    }

    public static void setDTraceEnabled(boolean b) {
        ok();
        dtraceEnabled = b;
    }

    public static void setMonitoringEnabled(boolean b) {
        ok();
        monitoringEnabled = b;
    }

    public static boolean isMonitoringEnabled() {
        ok();
        return monitoringEnabled;
    }

    public static boolean isDtraceAvailable() {
        ok();

        if (dt == null)
            return false;

        if (!dtraceEnabled)
            return false;

        if (!monitoringEnabled)
            return false;

        return true;
    }

    /** This only reveals whether it is enabled in domain.xml
     * isDtraceAvailable() checks this AND a few other things
     * @return
     */
    public static boolean isDtraceEnabled() {
        return dtraceEnabled;
    }

    static public DTraceContract getDtraceEngine() {
        return isDtraceAvailable() ? dt : null;
    }

    private static void setDTraceAvailabilty() {
        ok();
        dt = habitat.getService(DTraceContract.class);

        if (dt == null) {
            logDTraceAvailability(false, false);
        }
        else if (!dt.isSupported()) {
            dt = null;
            logDTraceAvailability(true, false);
        }
        // else dt is available!!
        else {
            logDTraceAvailability(true, true);
        }
    }

    /** bnevins -- I see 2 exact copies of this big chunk of code -- so I moved it here!
     * bnevins Oct 18, 2009 -- I can't see any reason why we FORCE users to annotate every single
     * parameter!   We should just make a name up if they don't provide one.  Since such
     * names  are not saved to the byte code it can't have any runtime effect.
     * @param method
     * @return
     */
    public static String[] getParamNames(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        String[] paramNames = new String[paramTypes.length];
        Annotation[][] allAnns = method.getParameterAnnotations();
        int index = 0;

        for (Annotation[] paramAnns : allAnns) {
            paramNames[index] = getParamName(paramAnns, paramTypes, index);
            ++index;
        }
        return paramNames;
    }

    /**
     * return the Methods in the clazz that are annotated as Probe.
     * Note that we use getMethods() not getDeclaredMethods()
     * This allows a hierarchy of Probe Providers
     * @param clazz
     * @return a List of legal Methods null will never be returned.
     */
    public static List<Method> getProbeMethods(Class<?> clazz) {
        List<Method> list = new LinkedList<Method>();

        for (Method m : clazz.getDeclaredMethods())
            if (m.getAnnotation(Probe.class) != null)
                list.add(m);

        return list;
    }

    public static boolean isLegalDtraceParam(Class clazz) {
        return isIntegral(clazz) || String.class.equals(clazz);
    }

    public static boolean isIntegral(Class clazz) {
        for (Class c : INTEGRAL_CLASSES) {
            if (c.equals(clazz))
                return true;
        }
        return false;
    }

    public static boolean isIntegralOrFloat(Class clazz) {
        for (Class c : INTEGRAL_FLOAT_CLASSES) {
            if (c.equals(clazz))
                return true;
        }
        return false;
    }

    /**
     * return true if they are the same -- ignoring boxing/unboxing
     * AND if they are "integrals"
     *
     * @param c1
     * @param c2
     * @return
     */
    public static boolean compareIntegral(Class c1, Class c2) {
        // first make sure they are both in the 12 element array of legal classes
        if (!isIntegral(c1) || !isIntegral(c2))
            return false;

        // next a sanity check -- they ought to be different classes but let's check anyways!
        if (c1.equals(c2))
            return true;

        if (c1.equals(short.class)) {
            return c2.equals(Short.class);
        }
        if (c1.equals(long.class)) {
            return c2.equals(Long.class);
        }
        if (c1.equals(int.class)) {
            return c2.equals(Integer.class);
        }
        if (c1.equals(byte.class)) {
            return c2.equals(Byte.class);
        }
        if (c1.equals(char.class)) {
            return c2.equals(Character.class);
        }
        if (c1.equals(boolean.class)) {
            return c2.equals(Boolean.class);
        }
        if (c2.equals(short.class)) {
            return c1.equals(Short.class);
        }
        if (c2.equals(long.class)) {
            return c1.equals(Long.class);
        }
        if (c2.equals(int.class)) {
            return c1.equals(Integer.class);
        }
        if (c2.equals(byte.class)) {
            return c1.equals(Byte.class);
        }
        if (c2.equals(char.class)) {
            return c1.equals(Character.class);
        }
        if (c2.equals(boolean.class)) {
            return c1.equals(Boolean.class);
        }

        // can't get here!!!
        return false;
    }

    public static boolean compareIntegralOrFloat(Class c1, Class c2) {
        // first make sure they are both in the 12 element array of legal classes
        if (!isIntegralOrFloat(c1) || !isIntegralOrFloat(c2))
            return false;

        // next a sanity check -- they ought to be different classes but let's check anyways!
        if (c1.equals(c2))
            return true;

        if (c1.equals(short.class)) {
            return c2.equals(Short.class);
        }
        if (c1.equals(long.class)) {
            return c2.equals(Long.class);
        }
        if (c1.equals(int.class)) {
            return c2.equals(Integer.class);
        }
        if (c1.equals(byte.class)) {
            return c2.equals(Byte.class);
        }
        if (c1.equals(char.class)) {
            return c2.equals(Character.class);
        }
        if (c1.equals(boolean.class)) {
            return c2.equals(Boolean.class);
        }
        if (c1.equals(float.class)) {
            return c2.equals(Float.class);
        }
        if (c1.equals(double.class)) {
            return c2.equals(Double.class);
        }
        if (c2.equals(short.class)) {
            return c1.equals(Short.class);
        }
        if (c2.equals(long.class)) {
            return c1.equals(Long.class);
        }
        if (c2.equals(int.class)) {
            return c1.equals(Integer.class);
        }
        if (c2.equals(byte.class)) {
            return c1.equals(Byte.class);
        }
        if (c2.equals(char.class)) {
            return c1.equals(Character.class);
        }
        if (c2.equals(boolean.class)) {
            return c1.equals(Boolean.class);
        }
        if (c2.equals(float.class)) {
            return c1.equals(Float.class);
        }
        if (c2.equals(double.class)) {
            return c1.equals(Double.class);
        }

        // can't get here!!!
        return false;
    }

    public static String makeName(FlashlightProbeProvider provider) {
        return makeName(provider.getModuleProviderName(),
                provider.getModuleName(),
                provider.getProbeProviderName());
    }

    public static String makeName(String a, String b, String c) {
        StringBuilder sb = new StringBuilder();

        sb.append(a);
        sb.append(":");
        sb.append(b);
        sb.append(":");
        sb.append(c);

        return sb.toString();
    }

    private static String getParamName(Annotation[] annotations, Class<?>[] paramTypes, int index) {
        String name = null;

        for (Annotation annotation : annotations) {
            if (annotation instanceof ProbeParam) {
                ProbeParam pp = (ProbeParam) annotation;
                name = pp.value();
            }
        }

        // If we do not find an annotated parameter -- the we simply make one up.
        // Just the index would make a unique name, but to make things a bit easier to
        // follow -- we prepend the number with the type.
        if (name == null) {
            name = paramTypes[index].getName().replace('.', '_') + "_arg" + index;
        }
        return name;
    }

    private static void logDTraceAvailability(boolean contractExists, boolean isSupported) {
        // There are three and only three possibilities:
        // 1. dt  is null
        // 2. dt is not null and isSupported() is false
        // 3. dt is not null and isSupported() is true

        // if dtrace is not enabled then don't harass user with noisy
        // DTrace messages

        if (!dtraceEnabled)
            return;

        String message;

        if (!contractExists) {
          logger.log(Level.INFO, FlashlightLoggerInfo.DTRACE_NOT_AVAILABLE);
        }
        else if (!isSupported) {
          logger.log(Level.INFO, FlashlightLoggerInfo.DTRACE_NOT_SUPPORTED);
        }
        else {
          logger.log(Level.INFO, FlashlightLoggerInfo.DTRACE_READY);
        }
    }

    /*
    Will replace special characters with ascii codes
    (to avoid problems during class creation)
     */
    public static String getUniqueInvokerId(String suffix) {
        StringBuilder sb = new StringBuilder("_");
        if (suffix != null) {
            suffix = suffix.replace(".", "-");
            suffix = suffix.replace(":", "-");
            suffix = suffix.replace("-", "_");
            String[] input = suffix.split("\\W");
            for (String str : input) {
                sb.append(str).append("_");
            }
        }

        return sb.toString();
    }

    private static void ok() {
        if (habitat == null || monConfig == null) {
            String errStr = localStrings.getLocalString("habitatNotSet", "Internal Error: habitat was not set in {0}", FlashlightUtils.class);
            throw new RuntimeException(errStr);
        }
    }
    private final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(FlashlightUtils.class);
    private static volatile ServiceLocator habitat;
    private static volatile MonitoringService monConfig;
    private static DTraceContract dt;
    private static boolean dtraceEnabled;
    private static boolean monitoringEnabled;
    private final static Object LOCK = new Object();
    private final static Class[] INTEGRAL_CLASSES = new Class[]{
        int.class, long.class, short.class, boolean.class, char.class, byte.class,
        Integer.class, Long.class, Short.class, Boolean.class, Character.class, Byte.class,};
    private final static Class[] INTEGRAL_FLOAT_CLASSES = new Class[]{
        int.class, long.class, short.class, boolean.class, char.class, byte.class,
        Integer.class, Long.class, Short.class, Boolean.class, Character.class, Byte.class,
        float.class, Float.class, double.class, Double.class,};
}

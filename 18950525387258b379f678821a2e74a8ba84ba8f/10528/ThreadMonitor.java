/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * ThreadMonitor.java
 *
 * Created on July 21, 2005, 11:50 AM
 */

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.i18n.StringManager;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import javax.management.MBeanServerConnection;

/**
 */
class ThreadMonitor {

    private final MBeanServerConnection mbsc;
    private final StringManager sm = StringManager.getManager(ThreadMonitor.class);
    private static final BigInteger S2NANOS = new BigInteger("" + 1000000000);
    public ThreadMonitor(final MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    public final String getThreadDump() {
        //final long start = System.currentTimeMillis();
        final StringBuilder sb = new StringBuilder();
        final StringBuilderNewLineAppender td = new StringBuilderNewLineAppender(sb);
        try {
            final ThreadMXBean tmx = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
            final String title = getTitle();
            td.append(title);
            td.append(sm.getString("thread.no", tmx.getThreadCount()));
            td.append(sm.getString("daemon.thread.no", tmx.getDaemonThreadCount()));
            td.append(sm.getString("peak.thread.no", tmx.getPeakThreadCount()));
            boolean tc = (tmx.isThreadContentionMonitoringSupported()) ? true : false;
            td.append(sm.getString("thread.contention.monitoring.supported", tc));
            boolean tce = (tmx.isThreadContentionMonitoringEnabled()) ? true : false;
            td.append(sm.getString("thread.contention.monitoring.enabled", tce));
            boolean cputs = (tmx.isThreadCpuTimeSupported()) ? true : false;
            td.append(sm.getString("thread.cputime.supported", cputs));
            boolean cpute = (tmx.isThreadCpuTimeEnabled()) ? true : false;
            td.append(sm.getString("thread.cputime.enabled", cpute));
            final long[] tids = tmx.getAllThreadIds();
            final ThreadInfo[] tinfos = tmx.getThreadInfo(tids, Integer.MAX_VALUE);
            /*
            Arrays.sort(tinfos, new Comparator<ThreadInfo> () {
                public int compare(ThreadInfo a, ThreadInfo b) {
                    return ( a.getThreadName().compareTo(b.getThreadName()) );
                }
            });
             */
            for (final ThreadInfo ti : tinfos) {
                td.append(dumpThread(tmx, ti));
            }
            sb.append(getDeadlockInfo(tmx));
            return ( td.toString() );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
        /*finally {
            final long end = System.currentTimeMillis();
            final double time = (end/1000.0) - (start/1000.0);
            //logger.info("Time in seconds to get the jvm thread dump: " + time);
        }*/
    }
    private String dumpThread(ThreadMXBean tmx, ThreadInfo ti) {
        String msg = "--------------------------------------------------------------------------------";
        final StringBuilder sb = new StringBuilder(msg).append(StringBuilderNewLineAppender.SEP);
        sb.append(sm.getString("execution.info")).append(StringBuilderNewLineAppender.SEP);
        sb.append("-----------------------").append(StringBuilderNewLineAppender.SEP);
        final long ids = ti.getThreadId();
        final String ss  = ti.getThreadState().toString();
        msg = sm.getString("thread.title", quote(ti.getThreadName()), ids, ss);
        sb.append(msg);
        if (ti.getLockName() != null) {
            msg = sm.getString("thread.waiting.on", ti.getLockName());
            sb.append(" " + msg);
        }
        if (ti.isSuspended()) {
            msg = sm.getString("thread.suspended");
            sb.append(" " + msg);
        }
        if (ti.isInNative()) {
            msg = sm.getString("thread.in.native");
            sb.append(" " + msg);
        }
        sb.append(StringBuilderNewLineAppender.SEP);
        for (final StackTraceElement ste : ti.getStackTrace()) {
            msg = sm.getString("thread.stack.element", ste.toString());
            sb.append(msg);
            sb.append(StringBuilderNewLineAppender.SEP);
        }
        msg = sm.getString("sync.info");
        sb.append(msg).append(StringBuilderNewLineAppender.SEP);
        sb.append("-----------------------").append(StringBuilderNewLineAppender.SEP);
        if (ti.getLockOwnerName() != null) {
            msg = sm.getString("lock.owner.details", ti.getLockOwnerName(), ti.getLockOwnerId());
            sb.append(msg).append(StringBuilderNewLineAppender.SEP);
        }
        msg = sm.getString("thread.blocked.times", ti.getBlockedCount());
        sb.append(msg).append(StringBuilderNewLineAppender.SEP);
        long bt = ti.getBlockedTime();
        if (bt != -1) { //if bt == -1 thread contention monitoring is not enabled, reported above
            msg = sm.getString("thread.blocked.totaltime", bt);
            sb.append(msg).append(StringBuilderNewLineAppender.SEP);
        }
        long wt = ti.getWaitedCount();
        msg = sm.getString("wait.times", wt);
        sb.append(msg).append(StringBuilderNewLineAppender.SEP);
        boolean tcput = tmx.isThreadCpuTimeEnabled() ? true : false;
        if (tcput) {
            long cput = tmx.getThreadCpuTime(ti.getThreadId());
            if (cput != -1) {
                BigInteger[] times = new BigInteger(cput + "").divideAndRemainder(S2NANOS);
                msg = sm.getString("thread.total.cpu.time", times[0], times[1]);
                sb.append(msg).append(StringBuilderNewLineAppender.SEP);
            }
            long user = tmx.getThreadUserTime(ti.getThreadId());
            if (user != -1) {
                BigInteger[] times = new BigInteger(cput + "").divideAndRemainder(S2NANOS);
                msg = sm.getString("thread.cpu.user.time", times[0], times[1]);
                sb.append(msg).append(StringBuilderNewLineAppender.SEP);
            }
        }
        msg = sm.getString("lock.owner.details", ti.getLockOwnerName(), ti.getLockOwnerId());
        msg = getMoreThreadInfo(ti, "getLockedMonitors");
        sb.append(sm.getString("monitor.info", msg)).append(StringBuilderNewLineAppender.SEP);
        msg = getMoreThreadInfo(ti, "getLockedSynchronizers");
        sb.append(sm.getString("ownable.sync.info", msg));
        return ( sb.toString() );
    }

    private String getTitle() throws Exception {
        final RuntimeMXBean rt  = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        final String vmname     = rt.getVmName();
        final String vmversion  = rt.getVmVersion();
        final String vmvendor   = rt.getVmVendor();
        final String title      = sm.getString("td.title", vmname, vmversion, vmvendor);

        return ( title );
    }

    private String quote(final String uq) {
        final StringBuilder sb = new StringBuilder("\"");
        sb.append(uq).append("\"");
        return ( sb.toString() );
    }

    private String getDeadlockInfo(final ThreadMXBean tmx) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        final long[] dts = tmx.findMonitorDeadlockedThreads();
        if (dts == null) {
            sb.append(sm.getString("no.deadlock"));
        }
        else {
            sb.append(sm.getString("deadlocks.found"));
            for (final long dt : dts) {
                final ThreadInfo ti = tmx.getThreadInfo(dt);
                sb.append(this.dumpThread(tmx, ti));
            }
        }
        return ( sb.toString() );
    }

    private String getMoreThreadInfo(ThreadInfo ti, String mn) {
        String ms = "";
        try {
            Method glmm = ti.getClass().getDeclaredMethod(mn, (Class[])null);
            Object monitors = glmm.invoke(ti, (Object[])null);
            if (monitors instanceof Object[]) {
                return ( Arrays.toString((Object[])monitors) );
            } else {
                return (NA);
            }
        } catch(Exception e) {
            return (NA);
        }
    }

    public static final String NA = "NOT_AVAILABLE";
}

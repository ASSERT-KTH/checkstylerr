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

 package org.glassfish.admin.amx.j2ee;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;

public interface StateManageable {

    /**
     * This state indicates that the SMO has been requested to start,
     * and is in the process of starting. On entering this state an SMO may generate
     * an event whose type value is "STATE". Event notification of the STARTING state
     * is optional for all managed objects that implement StateManageable.
     */
    int STATE_STARTING = 0;

    /**
     * This is the normal running state for an SMO. This state indicates
     * that the SMO is operational. On entering this state an SMO must generate an
     * event whose type value is "STATE". Event notification of the RUNNING state is
     * required for all managed objects that implement StateManageable.
     */
    int STATE_RUNNING = 1;

    /**
     * This state indicates that the SMO has been requested to stop,
     * and is in the process of stopping. On entering this state an SMO may generate
     * an event whose type value is "STATE". Event notification of the STOPPING state
     * is optional for all managed objects that implement StateManageable.
     */
    int STATE_STOPPING = 2;

    /**
     * This state indicates that the StateManageable Object has stopped
     * and can be restarted. On entering this state an SMO must generate an event
     * whose type value is "STATE". Event notification of the STOPPED state is
     * required by all managed objects that implement StateManageable.
     */
    int STATE_STOPPED = 3;

    /**
     * This state indicates that the StateManageable Object is in a failed
     * state and intervention is required to restore the managed object. On entering
     * this state an SMO must generate an event whose type value is "STATE". Event
     * notification of the FAILED state is required by all managed objects that
     * implement StateManageable.
     */
    int STATE_FAILED = 4;

    /**
     * The current state of this SMO. The SMO can be in one of the following states:
     * <ul>
     * <li>#STATE_STARTING</li>
     * <li>#STATE_RUNNING</li>
     * <li>#STATE_STOPPING</li>
     * <li>#STATE_STOPPED</li>
     * <li>#STATE_FAILED</li>
     * </ul>
     * <p>
     * Note that the Attribute name is case-sensitive
     * "state" as defined by JSR 77.
     */
    @ManagedAttribute
    int getstate();


    /**
     * The time that the managed object was started represented as a
     * long which value is the number of milliseconds since
     * January 1, 1970, 00:00:00.
     * <p>
     * Note that the Attribute name is case-sensitive
     * "startTime" as defined by JSR 77.
     */
    @ManagedAttribute
    long getstartTime();


    /**
     * Starts the SMO. This operation can be invoked only when the SMO
     * is in the STOPPED state. It causes the SMO to go into the STARTING
     * state initially, and if it completes successfully, the SMO will be in
     * the RUNNING state. Note that start() is not called on any of the child
     * SMOs that are registered with this SMO; it is the responsibility of the
     * calling application to start the child SMO if this is required.
     */
    @ManagedOperation
    void start();


    /**
     * Starts the SMO. This operation can only be invoked when the SMO is in the
     * STOPPED state. It causes the SMO to go into the STARTING state initially,
     * and if it completes successfully, the SMO will be in the RUNNING state.
     * startRecursive() is called on all the child SMOs registered with this SMO
     * that are in the STOPPED state. Stops the SMO. This operation can only be
     * invoked when the SMO is in the RUNNING or STARTING state. It causes stop()
     * to be called on all the child SMOs registered with this SMO that are in the
     * RUNNING or STARTING state. It causes the SMO to go into the STOPPING state
     * initially, and if it completes successfully, the SMO and all the child SMOs
     * will be in the STOPPED state. There is no stopRecursive() operation because
     * it is mandatory if an SMO is in the STOPPED state, that all its child SMOs
     * must also be in the STOPPED state.
     */
    @ManagedOperation
    void startRecursive();


    /**
     * Stops the SMO. This operation can only be invoked when the SMO is in
     * the RUNNING or STARTING state. It causes stop() to be called on all the
     * child SMOs registered with this SMO that are in the RUNNING or STARTING
     * state. It causes the SMO to go into the STOPPING state initially,
     * and if it completes successfully, the SMO and all the child SMOs will be
     * in the STOPPED state. There is no stopRecursive() operation because it is
     * mandatory if an SMO is in the STOPPED state, that all its child SMOs must
     * also be in the STOPPED state.
     */
    @ManagedOperation
    void stop();
}

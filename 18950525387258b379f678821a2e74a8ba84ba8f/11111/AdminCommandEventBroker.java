/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

/**
 * Events broker for AdminCommands. It can be used to inform everybody who listen. Any object can be event. If ReST
 * Provider is registered for particular type, it is also transfered to remote client.
 *
 * @author mmares
 */
public interface AdminCommandEventBroker<T> {

    /**
     * Local events are not transfered to remote listener using SSE
     */
    String LOCAL_EVENT_PREFIX = "local/";

    /**
     * Fire event under defined name. Any object can be event.
     *
     * @param name Event name. Listener is registered to some name.
     * @param event Any object can be event
     */
    void fireEvent(String name, Object event);

    /**
     * Fire event under name of event.getClass.getName().
     *
     * @param event Any object can be event.
     */
    void fireEvent(Object event);

    /**
     * Register Listener for admin command events.
     *
     * @param regexpForName listen to events with name valid to this regular expression.
     * @param listener Listener will be called
     */
    void registerListener(String regexpForName, AdminCommandListener<T> listener);

    /**
     * Remove registered listener.
     *
     * @param listener Listener to remove
     */
    void unregisterListener(AdminCommandListener listener);

    /**
     * Returns true if exist exists registered listener for given eventName
     */
    boolean listening(String eventName);

    /**
     * Pack of utility methods related to this instance of event broker.
     */
    EventBrokerUtils getUtils();

    /**
     * Place relevant for utility methods
     */
    public interface EventBrokerUtils {

        String USER_MESSAGE_NAME = "usermessage";

        void sendMessage(String message);

    }

    /**
     * Listener for AdminCommand events.
     *
     * @param <T> Type of event
     */
    public interface AdminCommandListener<T> {

        void onAdminCommandEvent(String name, T event);

    }

    public static class BrokerListenerRegEvent {
        public static final String EVENT_NAME_LISTENER_REG = LOCAL_EVENT_PREFIX + "listener/register";
        public static final String EVENT_NAME_LISTENER_UNREG = LOCAL_EVENT_PREFIX + "listener/unregister";

        private final AdminCommandEventBroker broker;
        private final AdminCommandListener listener;

        public BrokerListenerRegEvent(AdminCommandEventBroker broker, AdminCommandListener listener) {
            this.broker = broker;
            this.listener = listener;
        }

        public AdminCommandEventBroker getBroker() {
            return broker;
        }

        public AdminCommandListener getListener() {
            return listener;
        }

    }

}

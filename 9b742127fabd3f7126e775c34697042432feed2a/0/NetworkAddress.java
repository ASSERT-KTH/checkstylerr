/*
 * Copyright (c) 2017 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.core.utils;

import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * A convenient wrapper class around network primitives in Java.
 *
 * How IPv6 is handled:
 *
 * If a hostname is passed in as input instead of a literal IP address, then the class
 * relies on {@link InetAddress#getAllByName(String)} to perform a lookup. By default
 * the JVM prefers IPv4 for this lookup in dual stack environments, unless the system
 * property "java.net.preferIPv6Addresses" is set to true on startup. In this case,
 * IPv6 is preferred and automatically selected.
 *
 * Note that there is one specific case where if IPv6 is preferred in dual stack and
 * you are running Couchbase Server pre 5.1 which does not support IPv6, then you must
 * set the system property "com.couchbase.forceIPv4" to true so that the firstly
 * returned IPv6 address is ignored and we are looking for an IPv4 address to be
 * resolved as well. Obviously, in a single IPv6 stack environment working with older
 * Couchbase Server releases won't work.
 *
 * @author Michael Nitschinger
 * @since 1.4.6
 */
public class NetworkAddress {

    public static final String REVERSE_DNS_PROPERTY = "com.couchbase.allowReverseDns";

    public static final String FORCE_IPV4_PROPERTY = "com.couchbase.forceIPv4";

    /**
     * Flag which controls the usage of reverse dns
     */
    public static final boolean ALLOW_REVERSE_DNS = Boolean.parseBoolean(
        System.getProperty(REVERSE_DNS_PROPERTY, "true")
    );

    /**
     * Flag which controls if even if ipv6 is present, IPv4 should be
     * preferred.
     */
    public static final boolean FORCE_IPV4 = Boolean.parseBoolean(
        System.getProperty(FORCE_IPV4_PROPERTY, "true")
    );

    private final InetAddress inner;
    private final boolean createdFromHostname;
    private final boolean allowReverseDns;

    NetworkAddress(final String input, final boolean reverseDns) {
        try {
            InetAddress foundAddr = null;
            for (InetAddress addr : InetAddress.getAllByName(input)) {
                if (addr instanceof Inet6Address && FORCE_IPV4) {
                    continue;
                }
                foundAddr = addr;
                break;
            }
            if (foundAddr == null) {
                if (FORCE_IPV4) {
                    throw new IllegalArgumentException("No IPv4 address found for \"" + input + "\"");
                } else {
                    throw new IllegalArgumentException("No IPv4 or IPv6 address found for \"" + input + "\"");
                }
            }
            this.inner = foundAddr;
            this.createdFromHostname = !InetAddresses.isInetAddress(input);
            this.allowReverseDns = reverseDns;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not create NetworkAddress.", ex);
        }
    }

    private NetworkAddress(final String input) {
        this(input, ALLOW_REVERSE_DNS);
    }

    /**
     * Creates a new {@link NetworkAddress} from either a hostname or ip address literal.
     */
    public static NetworkAddress create(final String input) {
        return new NetworkAddress(input);
    }

    /**
     * Creates a new {@link NetworkAddress} for loopback.
     */
    public static NetworkAddress localhost() {
        return create("127.0.0.1");
    }

    /**
     * Returns the hostname for this network address.
     *
     * @return the hostname.
     */
    public String hostname() {
        if (canUseHostname()) {
            return inner.getHostName();
        } else {
            throw new IllegalStateException("NetworkAddress not created from hostname " +
                "and reverse dns lookup disabled!");
        }
    }

    /**
     * Helper method to check if it is save to use the hostname representation.
     *
     * @return true if it is, false otherwise.
     */
    private boolean canUseHostname() {
        return allowReverseDns || createdFromHostname;
    }

    /**
     * Returns the string IP representation for this network address.
     *
     * @return the IP address in string representation
     */
    public String address() {
        return InetAddresses.toAddrString(inner);
    }

    /**
     * Returns the hostname if available and if not returns the address.
     *
     * @return hostname or address, best effort.
     */
    public String nameOrAddress() {
        return canUseHostname() ? hostname() : address();
    }

    /**
     * Prints a safe representation of the hostname (if available) and the IP address.
     *
     * @return hostname and ip as a string
     */
    public String nameAndAddress() {
        String result = address();
        return canUseHostname() ? result + "/" + hostname() : result;
    }

    @Override
    public String toString() {
        return "NetworkAddress{" +
                 inner +
                ", fromHostname=" + createdFromHostname +
                ", reverseDns=" + allowReverseDns +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkAddress that = (NetworkAddress) o;

        return inner.equals(that.inner);
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }
}

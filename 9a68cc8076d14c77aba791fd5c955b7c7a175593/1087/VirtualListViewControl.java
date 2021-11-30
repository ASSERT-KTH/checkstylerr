/*
 * Copyright (c) 2000, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.ctl;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;

import java.io.IOException;

import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Request Control for virtual-list-view
 * as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-ietf-ldapext-ldapv3-vlv-09.txt">draft-ietf-ldapext-ldapv3-vlv-09.txt</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     VirtualListViewRequest ::= SEQUENCE {
 *         beforeCount    INTEGER (0 .. maxInt),
 *         afterCount     INTEGER (0 .. maxInt),
 *         CHOICE {
 *             byoffset [0] SEQUENCE {
 *                 offset          INTEGER (0 .. maxInt),
 *                 contentCount    INTEGER (0 .. maxInt)
 *             }
 *             greaterThanOrEqual [1] AssertionValue
 *         }
 *         contextID     OCTET STRING OPTIONAL
 *     }
 *
 * </pre>
 *
 * This control is always used in conjunction with the server-side sort control
 * (<a href="http://www.ietf.org/rfc/rfc2891.txt">RFC-2891</a>).
 *
 * @see VirtualListViewResponseControl
 * @see SortControl
 * @author Vincent Ryan
 */
final public class VirtualListViewControl extends BasicControl {

    /**
     * The virtual-list-view control's assigned object identifier
     * is 2.16.840.1.113730.3.4.9.
     */
    public static final String OID = "2.16.840.1.113730.3.4.9";

    /**
     * The number of entries before the target entry in a sublist.
     *
     * @serial
     */
    private int beforeCount;

    /**
     * The number of entries after the target entry in a sublist.
     *
     * @serial
     */
    private int afterCount;

    /**
     * An offset into the list.
     *
     * @serial
     */
    private int targetOffset = -1;

    /**
     * An estimate of the number of entries in the list.
     *
     * @serial
     */
    private int listSize;

    /**
     * Attribute value used to locate the target entry.
     * This value is compared to values of the attribute specified
     * as the primary sort key.
     *
     * Only String and byte[] values are currently supported.
     *
     * @serial
     */
    private Object targetAttrValue = null;

    /**
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = null;

    private static final long serialVersionUID = 7739016048653396131L;

    /**
     * Constructs a virtual-list-view control.
     *
     * Request a view of a portion of the list centered around a given
     * target entry. The position of the target entry is estimated as a
     * percentage of the list.
     *
     * @param targetPercentage The position of the target entry expressed as a
     *                       percentage of the list. For example, a value of
     *                       25 indicates that the target entry is at the
     *                       25 percent mark in the list.
     * @param viewSize       The number of entries to be returned in this
     *                       view of the list.
     * @param criticality    The control's criticality setting.
     * @exception          IllegalArgumentException if targetPercentage is
     *                       outside the range 0-100.
     * @exception            IOException If a BER encoding error occurs.
     */
    public VirtualListViewControl(int targetPercentage, int viewSize, boolean criticality) throws IOException {
        super(OID, criticality, null);

        if ((targetPercentage > 100) ||
            (targetPercentage < 0) ||
            (viewSize < 0)) {
            throw new IllegalArgumentException();
        }

        targetOffset = targetPercentage;
        listSize = 100;

        // viewSize includes the target entry
        if (viewSize > 0) {
            viewSize -= 1;
        }
        beforeCount = afterCount = viewSize / 2;

        // adjust afterCount when viewSize is odd
        if (viewSize != ((viewSize / 2) * 2)) {
            afterCount++;
        }
        super.value = setEncodedValue();
    }

    /**
     * Constructs a virtual-list-view control.
     *
     * Request a view of a portion of the list with the specified number of
     * entries before and after a given target entry. The target entry is
     * identified by means of an offset into the list.
     *
     * @param targetOffset The position of the target entry as an offset
     *                     into the list.
     * @param listSize    An estimate of the number of entries in the list.
     * @param beforeCount  The number of entries to be returned before the
     *                     target entry.
     * @param afterCount   The number of entries to be returned after the
     *                     target entry.
     * @param criticality  The control's criticality setting.
     * @exception          IllegalArgumentException if targetOffset, listSize,
     *                     beforeCount or afterCount are less than zero.
     * @exception          IOException If a BER encoding error occurs.
     */
    public VirtualListViewControl(int targetOffset, int listSize, int beforeCount, int afterCount, boolean criticality)
        throws IOException {

        super(OID, criticality, null);

        if ((targetOffset <0) || (listSize <0) ||(beforeCount <0) || (afterCount <0)){
            throw new IllegalArgumentException();
        }

        this.targetOffset = targetOffset;
        this.listSize = listSize;
        this.beforeCount = beforeCount;
        this.afterCount = afterCount;
        super.value = setEncodedValue();
    }

    /**
     * Constructs a virtual-list-view critical control.
     *
     * Request a view of a portion of the list centered around a given
     * target entry. The target entry is the first entry that is greater
     * than or equal to the specified attribute value. The value's
     * attribute ID is the primary sort key specified in the server-side
     * sort control.
     *
     * @param targetAttrValue  An attribute value used to locate the target
     *                         entry. Its attribute ID is that of the primary
     *                         sort key specified in the server-side sort
     *                         control.
     * @param viewSize         The number of entries to be returned in this
     *                         view of the list.
     * @param criticality      The control's criticality setting.
     * @exception            InvalidAttributeValueException if
     *                         targetAttrValue is neither a String nor a byte[].
     * @exception              IOException If a BER encoding error occurs.
     */
    public VirtualListViewControl(Object targetAttrValue, int viewSize, boolean criticality)
        throws InvalidAttributeValueException, IOException {

        super(OID, criticality, null);

        if ((targetAttrValue == null) ||
            (! ((targetAttrValue instanceof String) ||
                (targetAttrValue instanceof byte[])))) {
            throw new InvalidAttributeValueException();
        }
        this.targetAttrValue = targetAttrValue;

        if (viewSize < 0) {
            throw new IllegalArgumentException();
        }

        // viewSize includes the target entry
        if (viewSize > 0) {
            viewSize -= 1;
        }
        beforeCount = afterCount = viewSize / 2;

        // adjust afterCount when viewSize is odd
        if (viewSize != ((viewSize / 2) * 2)) {
            afterCount++;
        }
        super.value = setEncodedValue();
    }

    /**
     * Constructs a virtual-list-view control.
     *
     * Request a view of a portion of the list with the specified number of
     * entries before and after a given target entry. The target entry is
     * the first entry that is greater than or equal to the specified
     * attribute value. The value's attribute ID is the primary sort key
     * specified in the server-side sort control.
     *
     * @param targetAttrValue An attribute value used to locate the target
     *                        entry. Its attribute ID is that of the primary
     *                        sort key specified in the server-side sort
     *                        control.
     * @param beforeCount     The number of entries to be returned before the
     *                        target entry.
     * @param afterCount      The number of entries to be returned after the
     *                        target entry.
     * @param criticality     The control's criticality setting.
     * @exception           InvalidAttributeValueException if
     *                        targetAttrValue is neither a String nor a byte[].
     * @exception             IllegalArgumentException if beforeCount or
     *                        afterCount are less than zero.
     * @exception             IOException If a BER encoding error occurs.
     */
    public VirtualListViewControl(Object targetAttrValue, int beforeCount, int afterCount, boolean criticality)
        throws InvalidAttributeValueException, IOException {

        super(OID, criticality, null);

        if ((targetAttrValue == null) ||
            (! ((targetAttrValue instanceof String) ||
                (targetAttrValue instanceof byte[])))) {
            throw new InvalidAttributeValueException();
        }

        if ((beforeCount <0) || (afterCount <0)) {
            throw new IllegalArgumentException();
        }

        this.targetAttrValue = targetAttrValue;
        this.beforeCount = beforeCount;
        this.afterCount = afterCount;
        super.value = setEncodedValue();
    }

    /**
     * Sets a server-generated cookie in the virtual-list-view request.
     *
     * @param contextID A server-generated cookie.
     * @exception IOException If a BER encoding error occurs.
     */
    public void setContextID(byte[] contextID) throws IOException {
        if (this.cookie != contextID) {
            this.cookie = contextID;
            super.value = setEncodedValue(); // re-encode
        } else {
            this.cookie = contextID;
        }
    }

    /**
     * Sets the ASN.1 BER encoded value of the virtual-list-view control.
     * The result is the raw BER bytes including the tag and length of
     * the control's value. It does not include the controls OID or criticality.
     *
     * @return A possibly null byte array representing the ASN.1 BER encoded
     *         value of the LDAP sort control.
     * @exception IOException If a BER encoding error occurs.
     */
    private byte[] setEncodedValue() throws IOException {

        // build the ASN.1 encoding
        BerEncoder ber = new BerEncoder(64);

        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);

        ber.encodeInt(beforeCount);
        ber.encodeInt(afterCount);

        if (targetOffset >= 0) {
            ber.beginSeq(Ber.ASN_CONTEXT | Ber.ASN_CONSTRUCTOR | 0);
            ber.encodeInt(targetOffset);
            ber.encodeInt(listSize);
            ber.endSeq();

        } else {
            if (targetAttrValue instanceof String) {
                ber.encodeString((String)targetAttrValue,
                    (Ber.ASN_CONTEXT | 1), true);
            } else { // byte[]
                ber.encodeOctetString((byte[])targetAttrValue,
                    (Ber.ASN_CONTEXT | 1));
            }
        }
        if (cookie != null) {
            ber.encodeOctetString(cookie, Ber.ASN_OCTET_STR);
        }
        ber.endSeq();

        return ber.getTrimmedBuf();
    }
}

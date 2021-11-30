/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.iiop.security;


import com.sun.corba.ee.org.omg.GSSUP.GSSUPMechOID;
import com.sun.corba.ee.org.omg.CSI.GSS_NT_Export_Name_OID;
import com.sun.corba.ee.org.omg.CSI.GSS_NT_Scoped_Username_OID;

import java.util.Arrays;
import java.util.logging.*;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import com.sun.logging.*;

/*
 * @author    Sekhar Vajjhala
 * (Almost complete rewrite of an old version)
 *
 */
public class GSSUtils {
    private static final java.util.logging.Logger _logger = LogDomains.getLogger(GSSUtils.class, LogDomains.CORBA_LOGGER);

    public static final Oid GSSUP_MECH_OID;

    public static final Oid GSS_NT_EXPORT_NAME_OID;

    /*
     * GSS_NT_SCOPED_USERNAME_OID is currently not used by this class. It is defined here for the sake of completeness.
     */
    public static final Oid GSS_NT_SCOPED_USERNAME_OID;

    private static byte[] mech;

    static {

        int i; // index
        Oid x = null;

        /* Construct an ObjectIdentifer by extracting each OID */

        try {
            i = GSSUPMechOID.value.indexOf(':');
            x = new Oid(GSSUPMechOID.value.substring(i + 1));
        } catch (GSSException e) {
            x = null;
            _logger.log(Level.SEVERE, "iiop.IOexception", e);
        }
        GSSUP_MECH_OID = x;

        try {
            i = GSS_NT_Export_Name_OID.value.indexOf(':');
            x = new Oid(GSS_NT_Export_Name_OID.value.substring(i + 1));
        } catch (GSSException e) {
            x = null;
            _logger.log(Level.SEVERE, "iiop.IOexception", e);
        }
        GSS_NT_EXPORT_NAME_OID = x;

        try {
            i = GSS_NT_Scoped_Username_OID.value.indexOf(':');
            x = new Oid(GSS_NT_Scoped_Username_OID.value.substring(i + 1));
        } catch (GSSException e) {
            x = null;
            _logger.log(Level.SEVERE, "iiop.IOexception", e);
        }
        GSS_NT_SCOPED_USERNAME_OID = x;

        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "GSSUP_MECH_OID: " + dumpHex(getDER(GSSUP_MECH_OID)));
                _logger.log(Level.FINE, "GSS_NT_EXPORT_NAME_OID: " + dumpHex(getDER(GSS_NT_EXPORT_NAME_OID)));
                _logger.log(Level.FINE, "GSS_NT_SCOPED_USERNAME_OID: " + dumpHex(getDER(GSS_NT_SCOPED_USERNAME_OID)));
            }
        } catch (GSSException e) {
            _logger.log(Level.SEVERE, "iiop.IOexception", e);
        }

        try {
            mech = GSSUtils.getDER(GSSUtils.GSSUP_MECH_OID);
        } catch (GSSException io) {
            mech = null;
        }
    }

    // Dumps the hex values in the given byte array
    public static String dumpHex(byte[] octets) {
        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < octets.length; i++) {
            if ((i != 0) && ((i % 16) == 0))
                result.append("\n    ");
            int b = octets[i];
            if (b < 0)
                b = 256 + b;
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            result.append(hex + " ");
        }

        return result.toString();
    }

    /*
     * Import the exported name from the mechanism independent exported name.
     */

    public static byte[] importName(Oid oid, byte[] externalName) throws GSSException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Attempting to import mechanism independent name");
            _logger.log(Level.FINE, dumpHex(externalName));
        }

        GSSException e = new GSSException(GSSException.BAD_NAME);

        if (externalName[0] != 0x04)
            throw e;

        if (externalName[1] != 0x01)
            throw e;

        int mechoidlen = (((int) externalName[2]) << 8) + (externalName[3] & 0xff);

        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE, "Mech OID length = " + mechoidlen);
        if (externalName.length < (4 + mechoidlen + 4))
            throw e;

        /*
         * get the mechanism OID and verify it is the same as oid passed as an argument.
         */

        byte[] deroid = new byte[mechoidlen];
        System.arraycopy(externalName, 4, deroid, 0, mechoidlen);
        Oid oid1 = getOID(deroid);
        if (!oid1.equals((Object) oid))
            throw e;

        int pos = 4 + mechoidlen;

        int namelen = (((int) externalName[pos]) << 24) + (((int) externalName[pos + 1]) << 16) + (((int) externalName[pos + 2]) << 8)
                + (((int) externalName[pos + 3]));

        pos += 4; // start of the mechanism specific exported name

        if (externalName.length != (4 + mechoidlen + 4 + namelen))
            throw e;

        byte[] name = new byte[externalName.length - pos];
        System.arraycopy(externalName, pos, name, 0, externalName.length - pos);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Mechanism specific name:");
            _logger.log(Level.FINE, dumpHex(name));
            _logger.log(Level.FINE, "Successfully imported mechanism independent name");
        }
        return name;
    }

    /* verify if exportedName is of object ObjectIdentifier. */

    public static boolean verifyMechOID(Oid oid, byte[] externalName) throws GSSException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Attempting to verify mechanism independent name");
            _logger.log(Level.FINE, dumpHex(externalName));
        }

        GSSException e = new GSSException(GSSException.BAD_NAME);

        if (externalName[0] != 0x04)
            throw e;

        if (externalName[1] != 0x01)
            throw e;

        int mechoidlen = (((int) externalName[2]) << 8) + (externalName[3] & 0xff);

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Mech OID length = " + mechoidlen);
        }
        if (externalName.length < (4 + mechoidlen + 4))
            throw e;

        /*
         * get the mechanism OID and verify it is the same as oid passed as an argument.
         */

        byte[] deroid = new byte[mechoidlen];
        System.arraycopy(externalName, 4, deroid, 0, mechoidlen);
        Oid oid1 = getOID(deroid);
        if (!oid1.equals((Object) oid))
            return false;
        else
            return true;
    }

    /*
     * Generate an exported name as specified in [RFC 2743] section 3.2,
     * "Mechanism-Independent Exported Name Object Format". For convenience, the format of the exported name is reproduced
     * here from [RFC2743] :
     *
     * Format: Bytes 2 0x04 0x01 2 mech OID length (len) len mech OID's DER value 4 exported name len name len exported name
     *
     */
    public static byte[] createExportedName(Oid oid, byte[] extName) throws GSSException {
        byte[] oidDER = getDER(oid);
        int tokensize = 2 + 2 + oidDER.length + 4 + extName.length;

        byte[] token = new byte[tokensize];

        // construct the Exported Name
        int pos = 0;

        token[0] = 0x04;
        token[1] = 0x01;
        token[2] = (byte) (oidDER.length & 0xFF00);
        token[3] = (byte) (oidDER.length & 0x00FF);

        pos = 4;
        System.arraycopy(oidDER, 0, token, pos, oidDER.length);
        pos += oidDER.length;

        int namelen = extName.length;

        token[pos++] = (byte) (namelen & 0xFF000000);
        token[pos++] = (byte) (namelen & 0x00FF0000);
        token[pos++] = (byte) (namelen & 0x0000FF00);
        token[pos++] = (byte) (namelen & 0x000000FF);

        System.arraycopy(extName, 0, token, pos, namelen);

        return token;
    }

    /*
     * Return the DER representation of an ObjectIdentifier. The DER representation is as follows:
     *
     * 0x06 -- Tag for OBJECT IDENTIFIER derOID.length -- length in octets of OID DER value of OID -- written as specified
     * byte the DER representation for an ObjectIdentifier.
     */

    public static byte[] getDER(Oid id) throws GSSException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Returning OID in DER format");
            _logger.log(Level.FINE, "    OID = " + id.toString());
        }

        byte[] oid = id.getDER();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "    DER OID: " + dumpHex(oid));
        }

        return oid;
    }

    /*
     * Return the OID corresponding to an OID represented in DER format as follows:
     *
     * 0x06 -- Tag for OBJECT IDENTIFIER derOID.length -- length in octets of OID DER value of OID -- written as specified
     * byte the DER representation for an ObjectIdentifier.
     */

    public static Oid getOID(byte[] derOID) throws GSSException {
        return new Oid(derOID);
    }

    /*
     * Construct a mechanism level independent token as specified in section 3.1, [RFC 2743]. This consists of a token tag
     * followed byte a mechanism specific token. The format - here for convenience - is as follows:
     *
     * Token Tag Description
     *
     * 0x60 | Tag for [APPLICATION 0] SEQUENCE <token-length-octets> | 0x06 | Along with the next two entries
     * <object-identifier-length> | is a DER encoding of an object <object-identifier-octets> | identifier
     *
     * Mechanism specific token | format defined by the mechanism itself outside of RFC 2743.
     */

    public static byte[] createMechIndToken(Oid mechoid, byte mechtok[]) throws GSSException {
        byte[] deroid = getDER(mechoid);

        byte[] token = new byte[1 // for 0x60
                + getDERLengthSize(deroid.length + mechtok.length) + deroid.length + mechtok.length];
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Going to create a mechanism independent token");
        }
        int index = 0;

        token[index++] = 0x60;

        index = writeDERLength(token, index, deroid.length + mechtok.length);

        System.arraycopy(deroid, 0, token, index, deroid.length);

        index += deroid.length;
        System.arraycopy(mechtok, 0, token, index, mechtok.length);

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Mechanism independent token created: ");
            _logger.log(Level.FINE, dumpHex(token));
        }

        return token;
    }

    /*
     * Retrieve a mechanism specific token from a mechanism independent token. The format of a mechanism independent token
     * is specified in section 3.1, [RFC 2743].
     */

    public static byte[] getMechToken(Oid oid, byte[] token) {
        byte[] mechtoken = null;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Received mechanism independent token: ");
            _logger.log(Level.FINE, dumpHex(token));
        }

        try {
            int index = verifyTokenHeader(oid, token);
            int mechtoklen = token.length - index;
            mechtoken = new byte[mechtoklen];
            System.arraycopy(token, index, mechtoken, 0, mechtoklen);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Mechanism specific token : ");
                _logger.log(Level.FINE, dumpHex(mechtoken));
            }
        } catch (GSSException e) {
            _logger.log(Level.SEVERE, "iiop.IOexception", e);
        }
        return mechtoken;
    }

    /*
     * Verfies the header of a mechanism independent token. The header must be as specified in RFC 2743, section 3.1. The
     * header must contain an object identifier specified by the first parameter.
     *
     * If the header is well formed, then the starting position of the mechanism specific token within the token is
     * returned.
     *
     * If the header is mal formed, then an exception is thrown.
     */

    private static int verifyTokenHeader(Oid oid, byte[] token) throws GSSException {
        int index = 0;
        _logger.log(Level.FINE, "Attempting to verify tokenheader in the mechanism independent token.");

        // verify header
        if (token[index++] != 0x60)
            throw new GSSException(GSSException.DEFECTIVE_TOKEN);

        int toklen = readDERLength(token, index); // derOID length + token length

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Mech OID length + Mech specific length = " + toklen);
        }
        index += getDERLengthSize(toklen);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Mechanism OID index : " + index);
        }

        if (token[index] != 0x06)
            throw new GSSException(GSSException.DEFECTIVE_TOKEN);

        // add first two bytes to the MECH_OID_LEN
        int oidlen = token[index+1] + 2;
        byte[] buf = new byte[oidlen];

        System.arraycopy(token, index, buf, 0, oidlen);

        Oid mechoid = getOID(buf);

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Comparing mech OID in token with the expected mech OID");
            _logger.log(Level.FINE, "mech OID: " + dumpHex(getDER(mechoid)));
            _logger.log(Level.FINE, "expected mech OID: " + dumpHex(getDER(oid)));
        }

        if (!mechoid.equals((Object) oid)) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "mech OID in token does not match expected mech OID");
            }
            throw new GSSException(GSSException.DEFECTIVE_TOKEN);
        }
        int mechoidlen = getDER(oid).length;

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Mechanism specific token index : " + index + mechoidlen);
            _logger.log(Level.FINE, "Successfully verified header in the mechanism independent token.");
        }
        return (index + mechoidlen); // starting position of mech specific token
    }

    static int getDERLengthSize(int length) {
        if (length < (1 << 7))
            return (1);
        else if (length < (1 << 8))
            return (2);
        else if (length < (1 << 16))
            return (3);
        else if (length < (1 << 24))
            return (4);
        else
            return (5);
    }

    static int writeDERLength(byte[] token, int index, int length) {
        if (length < (1 << 7)) {
            token[index++] = (byte) length;
        } else {
            token[index++] = (byte) (getDERLengthSize(length) + 127);
            if (length >= (1 << 24))
                token[index++] = (byte) (length >> 24);
            if (length >= (1 << 16))
                token[index++] = (byte) ((length >> 16) & 0xff);
            if (length >= (1 << 8))
                token[index++] = (byte) ((length >> 8) & 0xff);
            token[index++] = (byte) (length & 0xff);
        }
        return (index);
    }

    static int readDERLength(byte[] token, int index) {
        byte sf;
        int ret = 0;
        int nooctets;

        sf = token[index++];

        if ((sf & 0x80) == 0x80) { // value > 128
            // bit 8 is 1 ; bits 0-7 of first bye is the number of octets
            nooctets = (sf & 0x7f); // remove the 8th bit
            for (; nooctets != 0; nooctets--)
                ret = (ret << 8) + (token[index++] & 0x00FF);
        } else
            ret = sf;

        return (ret);
    }

    /**
     * Return the ASN.1 encoded representation of a GSS mechanism identifier. Currently only the GSSUP Mechanism is
     * supported.
     */
    public static byte[] getMechanism() {
        byte[] mechCopy = Arrays.copyOf(mech, mech.length);
        return mechCopy;
    }

    public static void main(String[] args) {
        try {
            byte[] len = new byte[3];
            len[0] = (byte) 0x82;
            len[1] = (byte) 0x01;
            len[2] = (byte) 0xd3;
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Length byte array : " + dumpHex(len));
                _logger.log(Level.FINE, " Der length = " + readDERLength(len, 0));
            }
            String name = "default";
            byte[] externalName = createExportedName(GSSUtils.GSSUP_MECH_OID, name.getBytes());
            byte[] m = importName(GSSUtils.GSSUP_MECH_OID, externalName);
            if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE, "BAR:" + new String(m));
            String msg = "dummy_gss_export_sec_context";
            byte[] foo = createMechIndToken(GSSUtils.GSSUP_MECH_OID, msg.getBytes());
            if (_logger.isLoggable(Level.FINE))
                _logger.log(Level.FINE, "FOO:" + dumpHex(foo));
            byte[] msg1 = getMechToken(GSSUtils.GSSUP_MECH_OID, foo);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "BAR:" + dumpHex(msg1));
                _logger.log(Level.FINE, "BAR string: " + new String(msg1));
            }
        } catch (Exception e) {
            _logger.log(Level.SEVERE, "iiop.name_exception", e);
        }
    }

}

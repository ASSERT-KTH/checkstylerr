/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal;

import java.io.*;
import java.nio.ByteBuffer;

public class GFBase64Encoder { // java.util.Base64 is private constructor and so moving/reusing sun.misc.BASE64Encoder

    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    protected PrintStream pStream;

    protected void encodeBufferPrefix(OutputStream var1) throws IOException {
        this.pStream = new PrintStream(var1);
    }

    protected void encodeBufferSuffix(OutputStream var1) throws IOException {
    }

    protected void encodeLinePrefix(OutputStream var1, int var2) throws IOException {
    }

    protected void encodeLineSuffix(OutputStream var1) throws IOException {
        this.pStream.println();
    }

    protected int readFully(InputStream var1, byte[] var2) throws IOException {
        for(int var3 = 0; var3 < var2.length; ++var3) {
            int var4 = var1.read();
            if(var4 == -1) {
                return var3;
            }

            var2[var3] = (byte)var4;
        }

        return var2.length;
    }

    public void encode(InputStream var1, OutputStream var2) throws IOException {
        byte[] var5 = new byte[this.bytesPerLine()];
        this.encodeBufferPrefix(var2);

        while(true) {
            int var4 = this.readFully(var1, var5);
            if(var4 == 0) {
                break;
            }

            this.encodeLinePrefix(var2, var4);

            for(int var3 = 0; var3 < var4; var3 += this.bytesPerAtom()) {
                if(var3 + this.bytesPerAtom() <= var4) {
                    this.encodeAtom(var2, var5, var3, this.bytesPerAtom());
                } else {
                    this.encodeAtom(var2, var5, var3, var4 - var3);
                }
            }

            if(var4 < this.bytesPerLine()) {
                break;
            }

            this.encodeLineSuffix(var2);
        }

        this.encodeBufferSuffix(var2);
    }

    public void encode(byte[] var1, OutputStream var2) throws IOException {
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);
        this.encode((InputStream)var3, var2);
    }

    public String encode(byte[] var1) {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);
        String var4 = null;

        try {
            this.encode((InputStream)var3, var2);
            var4 = var2.toString("8859_1");
            return var4;
        } catch (Exception var6) {
            throw new Error("CharacterEncoder.encode internal error");
        }
    }

    private byte[] getBytes(ByteBuffer var1) {
        byte[] var2 = null;
        if(var1.hasArray()) {
            byte[] var3 = var1.array();
            if(var3.length == var1.capacity() && var3.length == var1.remaining()) {
                var2 = var3;
                var1.position(var1.limit());
            }
        }

        if(var2 == null) {
            var2 = new byte[var1.remaining()];
            var1.get(var2);
        }

        return var2;
    }

    public void encode(ByteBuffer var1, OutputStream var2) throws IOException {
        byte[] var3 = this.getBytes(var1);
        this.encode(var3, var2);
    }

    public String encode(ByteBuffer var1) {
        byte[] var2 = this.getBytes(var1);
        return this.encode(var2);
    }

    public void encodeBuffer(InputStream var1, OutputStream var2) throws IOException {
        byte[] var5 = new byte[this.bytesPerLine()];
        this.encodeBufferPrefix(var2);

        int var4;
        do {
            var4 = this.readFully(var1, var5);
            if(var4 == 0) {
                break;
            }

            this.encodeLinePrefix(var2, var4);

            for(int var3 = 0; var3 < var4; var3 += this.bytesPerAtom()) {
                if(var3 + this.bytesPerAtom() <= var4) {
                    this.encodeAtom(var2, var5, var3, this.bytesPerAtom());
                } else {
                    this.encodeAtom(var2, var5, var3, var4 - var3);
                }
            }

            this.encodeLineSuffix(var2);
        } while(var4 >= this.bytesPerLine());

        this.encodeBufferSuffix(var2);
    }

    public void encodeBuffer(byte[] var1, OutputStream var2) throws IOException {
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);
        this.encodeBuffer((InputStream)var3, var2);
    }

    public String encodeBuffer(byte[] var1) {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);

        try {
            this.encodeBuffer((InputStream)var3, var2);
        } catch (Exception var5) {
            throw new Error("CharacterEncoder.encodeBuffer internal error");
        }

        return var2.toString();
    }

    public void encodeBuffer(ByteBuffer var1, OutputStream var2) throws IOException {
        byte[] var3 = this.getBytes(var1);
        this.encodeBuffer(var3, var2);
    }

    public String encodeBuffer(ByteBuffer var1) {
        byte[] var2 = this.getBytes(var1);
        return this.encodeBuffer(var2);
    }

    protected int bytesPerAtom() {
        return 3;
    }

    protected int bytesPerLine() {
        return 57;
    }

    protected void encodeAtom(OutputStream var1, byte[] var2, int var3, int var4) throws IOException {
        byte var5;
        if(var4 == 1) {
            var5 = var2[var3];
            byte var6 = 0;
            boolean var7 = false;
            var1.write(pem_array[var5 >>> 2 & 63]);
            var1.write(pem_array[(var5 << 4 & 48) + (var6 >>> 4 & 15)]);
            var1.write(61);
            var1.write(61);
        } else {
            byte var8;
            if(var4 == 2) {
                var5 = var2[var3];
                var8 = var2[var3 + 1];
                byte var9 = 0;
                var1.write(pem_array[var5 >>> 2 & 63]);
                var1.write(pem_array[(var5 << 4 & 48) + (var8 >>> 4 & 15)]);
                var1.write(pem_array[(var8 << 2 & 60) + (var9 >>> 6 & 3)]);
                var1.write(61);
            } else {
                var5 = var2[var3];
                var8 = var2[var3 + 1];
                byte var10 = var2[var3 + 2];
                var1.write(pem_array[var5 >>> 2 & 63]);
                var1.write(pem_array[(var5 << 4 & 48) + (var8 >>> 4 & 15)]);
                var1.write(pem_array[(var8 << 2 & 60) + (var10 >>> 6 & 3)]);
                var1.write(pem_array[var10 & 63]);
            }
        }

    }
}

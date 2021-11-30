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

package com.sun.jdo.api.persistence.enhancer.classfile;


import java.io.PrintStream;

/**
 * Special instruction form for the opc_tableswitch instruction
 */
public class InsnTableSwitch extends Insn {
    /* The lowest value in the jump table */
    private int lowOp;

    /* The default target for the switch */
    private InsnTarget defaultOp;

    /* The targets for the switch - a switch value of lowOp dispatches
     * to targetsOp[0], lowOp+1 dispatches to targetsOp[1], etc. */
    private InsnTarget[] targetsOp;

    /* public accessors */

    public int nStackArgs() {
        return 1;
    }

    public int nStackResults() {
        return 0;
    }

    public String argTypes() {
        return "I";//NOI18N
    }

    public String resultTypes() {
        return "";//NOI18N
    }

    public boolean branches() {
        return true;
    }

    /**
     * Mark possible branch targets
     */
    public void markTargets() {
        defaultOp.setBranchTarget();
        for (int i=0; i<targetsOp.length; i++)
            targetsOp[i].setBranchTarget();
    }


    /**
     * Return the lowest case for the switch
     */
    public int lowCase() {
        return lowOp;
    }

    /**
     * Return the defaultTarget for the switch
     */
    public InsnTarget defaultTarget() {
        return defaultOp;
    }

    /**
     * Return the targets for the cases of the switch.
     */
    public InsnTarget[] switchTargets() {
        return targetsOp;
    }

    /**
     * Constructor for opc_tableswitch
     */
    //@olsen: made public
    public InsnTableSwitch(int lowOp, InsnTarget defaultOp,
        InsnTarget[] targetsOp) {
        this(lowOp, defaultOp, targetsOp, NO_OFFSET);
    }


    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_tableswitch  ");//NOI18N
        for (int i=0; i<targetsOp.length; i++) {
            int index = i + lowOp;
            if (targetsOp[i].offset() != defaultOp.offset()) {
                ClassPrint.spaces(out, indent+2);
                out.println(index + " -> " + targetsOp[i].offset());//NOI18N
            }
        }
        ClassPrint.spaces(out, indent+2);
        out.println("default -> " + defaultOp.offset());//NOI18N
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = (index + 3) & ~3;
        index = storeInt(buf, index, defaultOp.offset() - offset());
        index = storeInt(buf, index, lowOp);
        index = storeInt(buf, index, lowOp+targetsOp.length-1);
        for (int i=0; i<targetsOp.length; i++)
            index = storeInt(buf, index, targetsOp[i].offset() - offset());
        return index;
    }

    int size() {
        /* account for the instruction, 0-3 bytes of pad, 3 ints */
        int basic = ((offset() + 4) & ~3) - offset() + 12;
        /* Add 4*number of offsets */
        return basic + targetsOp.length*4;
    }


    InsnTableSwitch(int lowOp, InsnTarget defaultOp,
        InsnTarget[] targetsOp, int offset) {
        super(opc_tableswitch, offset);

        this.lowOp = lowOp;
        this.defaultOp = defaultOp;
        this.targetsOp = targetsOp;

        if (defaultOp == null || targetsOp == null)
            throw new InsnError ("attempt to create an opc_tableswitch" +//NOI18N
                " with invalid operands");//NOI18N
    }

    static InsnTableSwitch read (InsnReadEnv insnEnv, int myPC) {
        /* eat up any padding */
        int thisPC = myPC +1;
        for (int pads = ((thisPC + 3) & ~3) - thisPC; pads > 0; pads--)
            insnEnv.getByte();
        InsnTarget defaultTarget = insnEnv.getTarget(insnEnv.getInt() + myPC);
        int low = insnEnv.getInt();
        int high = insnEnv.getInt();
        InsnTarget[] offsets = new InsnTarget[high - low + 1];
        for (int i=0; i<offsets.length; i++)
            offsets[i] = insnEnv.getTarget(insnEnv.getInt() + myPC);
        return new InsnTableSwitch(low, defaultTarget,   offsets, myPC);
    }
}

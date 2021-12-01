/*
 * Copyright (C) 2013-2015 RoboVM AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bugvm.apple.coregraphics;

/*<imports>*/
import java.io.*;
import java.nio.*;
import java.util.*;
import com.bugvm.objc.*;
import com.bugvm.objc.annotation.*;
import com.bugvm.objc.block.*;
import com.bugvm.rt.*;
import com.bugvm.rt.annotation.*;
import com.bugvm.rt.bro.*;
import com.bugvm.rt.bro.annotation.*;
import com.bugvm.rt.bro.ptr.*;
import com.bugvm.apple.corefoundation.*;
import com.bugvm.apple.foundation.*;
import com.bugvm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*//*</annotations>*/
public enum /*<name>*/CGBlendMode/*</name>*/ implements ValuedEnum {
    /*<values>*/
    Normal(0L),
    Multiply(1L),
    Screen(2L),
    Overlay(3L),
    Darken(4L),
    Lighten(5L),
    ColorDodge(6L),
    ColorBurn(7L),
    SoftLight(8L),
    HardLight(9L),
    Difference(10L),
    Exclusion(11L),
    Hue(12L),
    Saturation(13L),
    Color(14L),
    Luminosity(15L),
    Clear(16L),
    Copy(17L),
    SourceIn(18L),
    SourceOut(19L),
    SourceAtop(20L),
    DestinationOver(21L),
    DestinationIn(22L),
    DestinationOut(23L),
    DestinationAtop(24L),
    XOR(25L),
    PlusDarker(26L),
    PlusLighter(27L);
    /*</values>*/

    private final long n;

    private /*<name>*/CGBlendMode/*</name>*/(long n) { this.n = n; }
    public long value() { return n; }
    public static /*<name>*/CGBlendMode/*</name>*/ valueOf(long n) {
        for (/*<name>*/CGBlendMode/*</name>*/ v : values()) {
            if (v.n == n) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + n + " found in " 
            + /*<name>*/CGBlendMode/*</name>*/.class.getName());
    }
}

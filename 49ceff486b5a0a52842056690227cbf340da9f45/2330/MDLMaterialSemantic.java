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
package com.bugvm.apple.modelio;

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
import com.bugvm.apple.foundation.*;
import com.bugvm.apple.coregraphics.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Marshaler(ValuedEnum.AsMachineSizedUIntMarshaler.class)/*</annotations>*/
public enum /*<name>*/MDLMaterialSemantic/*</name>*/ implements ValuedEnum {
    /*<values>*/
    BaseColor(0L),
    Subsurface(1L),
    Metallic(2L),
    Specular(3L),
    SpecularExponent(4L),
    SpecularTint(5L),
    Roughness(6L),
    Anisotropic(7L),
    AnisotropicRotation(8L),
    Sheen(9L),
    SheenTint(10L),
    Clearcoat(11L),
    ClearcoatGloss(12L),
    Emission(13L),
    Bump(14L),
    Opacity(15L),
    InterfaceIndexOfRefraction(16L),
    MaterialIndexOfRefraction(17L),
    ObjectSpaceNormal(18L),
    TangentSpaceNormal(19L),
    Displacement(20L),
    DisplacementScale(21L),
    AmbientOcclusion(22L),
    AmbientOcclusionScale(23L),
    None(32768L),
    UserDefined(32769L);
    /*</values>*/

    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<methods>*//*</methods>*/

    private final long n;

    private /*<name>*/MDLMaterialSemantic/*</name>*/(long n) { this.n = n; }
    public long value() { return n; }
    public static /*<name>*/MDLMaterialSemantic/*</name>*/ valueOf(long n) {
        for (/*<name>*/MDLMaterialSemantic/*</name>*/ v : values()) {
            if (v.n == n) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + n + " found in " 
            + /*<name>*/MDLMaterialSemantic/*</name>*/.class.getName());
    }
}

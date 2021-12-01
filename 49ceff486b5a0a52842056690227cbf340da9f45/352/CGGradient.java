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
/*<annotations>*/@Library("CoreGraphics")/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CGGradient/*</name>*/ 
    extends /*<extends>*/CFType/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class CGGradientPtr extends Ptr<CGGradient, CGGradientPtr> {}/*</ptr>*/
    /*<bind>*/static { Bro.bind(CGGradient.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected CGGradient() {}
    /*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*//*</members>*/
    public static CGGradient create(CGColorSpace space, double[] components, double[] locations) {
        if (space == null) {
            throw new NullPointerException("space");
        }
        if (components == null) {
            throw new NullPointerException("components");
        }
        return create(space, 
                VM.getArrayValuesAddress(CoreGraphics.toMachineSizedFloatArray(components)), 
                locations != null ? VM.getArrayValuesAddress(CoreGraphics.toMachineSizedFloatArray(locations)) : 0, 
                locations != null ? locations.length : 0);
    }
    public static CGGradient create(CGColorSpace space, float[] components, float[] locations) {
        if (space == null) {
            throw new NullPointerException("space");
        }
        if (components == null) {
            throw new NullPointerException("components");
        }
        return create(space, 
                VM.getArrayValuesAddress(CoreGraphics.toMachineSizedFloatArray(components)), 
                locations != null ? VM.getArrayValuesAddress(CoreGraphics.toMachineSizedFloatArray(locations)) : 0, 
                locations != null ? locations.length : 0);
    }
    public static CGGradient create(CGColorSpace space, CGColor[] colors, double[] locations) {
        return create(space, colors, (Object) locations);
    }
    public static CGGradient create(CGColorSpace space, CGColor[] colors, float[] locations) {
        return create(space, colors, (Object) locations);
    }
    private static CGGradient create(CGColorSpace space, CGColor[] colors, Object locations) {
        if (colors == null) {
            throw new NullPointerException("colors");
        }
        try (CFArray colorsArray = CFArray.create(colors)) {
            return create(space, colorsArray, 
                    locations != null ? VM.getArrayValuesAddress(CoreGraphics.toMachineSizedFloatArray(locations)) : 0);
        }
    }
    /*<methods>*/
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGGradientGetTypeID", optional=true)
    public static native @MachineSizedUInt long getClassTypeID();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGGradientCreateWithColorComponents", optional=true)
    private static native CGGradient create(CGColorSpace space, @Pointer long components, @Pointer long locations, @MachineSizedUInt long count);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGGradientCreateWithColors", optional=true)
    private static native CGGradient create(CGColorSpace space, CFArray colors, @Pointer long locations);
    /*</methods>*/
}

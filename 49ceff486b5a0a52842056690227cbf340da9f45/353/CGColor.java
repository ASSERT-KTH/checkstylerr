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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CGColor/*</name>*/ 
    extends /*<extends>*/CFType/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<?> toObject(Class<? extends CFType> cls, long handle, long flags) {
            CFArray o = (CFArray) CFType.Marshaler.toObject(CFArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            return o.toList(CGColor.class);
        }
        @MarshalsPointer
        public static long toNative(List<? extends CFType> l, long flags) {
            if (l == null) {
                return 0L;
            }
            CFArray o = null;
            if (l instanceof CFArray) {
                o = (CFArray) l;
            } else {
                o = CFArray.create((List<? extends CFType>) l);
            }
            return CFType.Marshaler.toNative(o, flags);
        }
    }
    
    /*<ptr>*/public static class CGColorPtr extends Ptr<CGColor, CGColorPtr> {}/*</ptr>*/
    /*<bind>*/static { Bro.bind(CGColor.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected CGColor() {}
    /*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*//*</members>*/
    public static CGColor create(CGColorSpace space, float[] components) {
        return create(space, components, components != null ? components.length : 0);
    }
    public static CGColor create(CGColorSpace space, double[] components) {
        return create(space, components, components != null ? components.length : 0);
    }
    private static CGColor create(CGColorSpace space, Object components, int componentsLength) {
        if (space == null) {
            throw new NullPointerException("space == null");
        }
        if (components == null) {
            throw new NullPointerException("components == null");
        }
        if (componentsLength != space.getNumberOfComponents() + 1) {
            throw new IllegalArgumentException("components.length != space.numberOfComponents + 1");
        }
        return create(space, VM.getArrayValuesAddress(CoreGraphics.toMachineSizedFloatArray(components)));
    }
    
    public static CGColor create(CGColorSpace space, CGPattern pattern, float[] components) {
        return create(space, pattern, components, components != null ? components.length : 0);
    }
    public static CGColor create(CGColorSpace space, CGPattern pattern, double[] components) {
        return create(space, pattern, components, components != null ? components.length : 0);
    }
    private static CGColor create(CGColorSpace space, CGPattern pattern, Object components, int componentsLength) {
        if (space == null) {
            throw new NullPointerException("space == null");
        }
        if (components == null) {
            throw new NullPointerException("components == null");
        }
        if (componentsLength != space.getNumberOfComponents() + 1) {
            throw new IllegalArgumentException("components.length != space.numberOfComponents + 1");
        }
        return create(space, pattern, VM.getArrayValuesAddress(CoreGraphics.toMachineSizedFloatArray(components)));
    }
    
    public double[] getComponentsD() {
        return getComponents(this).toDoubleArray((int) getNumberOfComponents());
    }
    public float[] getComponentsF() {
        return getComponents(this).toFloatArray((int) getNumberOfComponents());
    }
    /*<methods>*/
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorCreate", optional=true)
    private static native @com.bugvm.rt.bro.annotation.Marshaler(CFType.NoRetainMarshaler.class) CGColor create(CGColorSpace space, @Pointer long components);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorCreateWithPattern", optional=true)
    private static native @com.bugvm.rt.bro.annotation.Marshaler(CFType.NoRetainMarshaler.class) CGColor create(CGColorSpace space, CGPattern pattern, @Pointer long components);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorCreateCopy", optional=true)
    public static native @com.bugvm.rt.bro.annotation.Marshaler(CFType.NoRetainMarshaler.class) CGColor createCopy(CGColor color);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorCreateCopyWithAlpha", optional=true)
    public static native @com.bugvm.rt.bro.annotation.Marshaler(CFType.NoRetainMarshaler.class) CGColor createCopy(CGColor color, @MachineSizedFloat double alpha);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorEqualToColor", optional=true)
    public native boolean equalsTo(CGColor color2);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorGetNumberOfComponents", optional=true)
    public native @MachineSizedUInt long getNumberOfComponents();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorGetComponents", optional=true)
    private static native MachineSizedFloatPtr getComponents(CGColor color);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorGetAlpha", optional=true)
    public native @MachineSizedFloat double getAlpha();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorGetColorSpace", optional=true)
    public native CGColorSpace getColorSpace();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorGetPattern", optional=true)
    public native CGPattern getPattern();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGColorGetTypeID", optional=true)
    public static native @MachineSizedUInt long getClassTypeID();
    /*</methods>*/
}

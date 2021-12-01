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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CGFunction/*</name>*/ 
    extends /*<extends>*/CFType/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class CGFunctionPtr extends Ptr<CGFunction, CGFunctionPtr> {}/*</ptr>*/
    
    public interface Evaluate {
        void evaluate(float[] inData, float[] outData);
    }
    
    static class Info {
        long domainDimension;
        long rangeDimension;
        Evaluate evaluate;
    }
    
    /*<bind>*/static { Bro.bind(CGFunction.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    
    private static java.util.concurrent.atomic.AtomicLong infoId = new java.util.concurrent.atomic.AtomicLong();
    private static final LongMap<Info> infos = new LongMap<>();
    private static final java.lang.reflect.Method cbEvaluate;
    private static final java.lang.reflect.Method cbReleaseInfo;
    
    static {
        try {
            cbEvaluate = CGFunction.class.getDeclaredMethod("cbEvaluate", long.class, FloatPtr.class, FloatPtr.class);
            cbReleaseInfo = CGFunction.class.getDeclaredMethod("cbReleaseInfo", long.class);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
    
    /*<constructors>*/
    protected CGFunction() {}
    /*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*//*</members>*/
    public static CGFunction create(long domainDimension, double[] domain, long rangeDimension, double[] range, Evaluate evaluate) {
        return create(domainDimension, domain, domain != null ? domain.length : 0, 
                rangeDimension, range, range != null ? range.length : 0, evaluate);
    }
    public static CGFunction create(long domainDimension, float[] domain, long rangeDimension, float[] range, Evaluate evaluate) {
        return create(domainDimension, domain, domain != null ? domain.length : 0, 
                rangeDimension, range, range != null ? range.length : 0, evaluate);
    }
    private static CGFunction create(long domainDimension, Object domain, int domainLength, 
            long rangeDimension, Object range, int rangeLength, Evaluate evaluate) {
        if (domain != null && domainLength != 2 * domainDimension) {
            throw new IllegalArgumentException("domain.length != 2 * domainDimension");
        }
        if (range != null && rangeLength != 2 * rangeDimension) {
            throw new IllegalArgumentException("range.length != 2 * rangeDimension");
        }
        if (evaluate == null) {
            throw new NullPointerException("callback");
        }
        
        MachineSizedFloatPtr domainPtr = null;
        if (domain != null) {
            domainPtr = Struct.allocate(MachineSizedFloatPtr.class, domainLength);
            if (domain instanceof double[]) {
                domainPtr.set((double[]) domain);
            } else {
                domainPtr.set((float[]) domain);
            }
        }
        MachineSizedFloatPtr rangePtr = null;
        if (range != null) {
            rangePtr = Struct.allocate(MachineSizedFloatPtr.class, rangeLength);
            if (range instanceof double[]) {
                rangePtr.set((double[]) range);
            } else {
                rangePtr.set((float[]) range);
            }
        }
        
        Info info = new Info();
        info.domainDimension = domainDimension;
        info.rangeDimension = rangeDimension;
        info.evaluate = evaluate;
        
        CGFunctionCallbacks callbacks = new CGFunctionCallbacks();
        callbacks.setEvaluate(new FunctionPtr(cbEvaluate));
        callbacks.setReleaseInfo(new FunctionPtr(cbReleaseInfo));
        
        long infoId = CGFunction.infoId.getAndIncrement();
        CGFunction result = create(infoId, domainDimension, domainPtr, rangeDimension, rangePtr, callbacks);
        if (result != null) {
            synchronized (infos) {
                infos.put(infoId, info);
            }
        }
        
        return result;
    }
    
    @Callback
    private static void cbEvaluate(@Pointer long infoId, FloatPtr inDataPtr, FloatPtr outDataPtr) {
        Info info = null;
        synchronized (infos) {
            info = infos.get(infoId);
        }
        float[] inData = inDataPtr.toFloatArray((int) info.domainDimension);
        float[] outData = new float[(int) info.rangeDimension];
        info.evaluate.evaluate(inData, outData);
        outDataPtr.set(outData);
    }
    @Callback
    private static void cbReleaseInfo(@Pointer long infoId) {
        synchronized (infos) {
            infos.remove(infoId);
        }
    }
    /*<methods>*/
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGFunctionGetTypeID", optional=true)
    public static native @MachineSizedUInt long getClassTypeID();
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Bridge(symbol="CGFunctionCreate", optional=true)
    private static native @com.bugvm.rt.bro.annotation.Marshaler(CFType.NoRetainMarshaler.class) CGFunction create(@Pointer long info, @MachineSizedUInt long domainDimension, MachineSizedFloatPtr domain, @MachineSizedUInt long rangeDimension, MachineSizedFloatPtr range, CGFunctionCallbacks callbacks);
    /*</methods>*/
}

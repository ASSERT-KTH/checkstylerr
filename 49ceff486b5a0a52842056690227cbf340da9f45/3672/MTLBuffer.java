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
package com.bugvm.apple.metal;

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
import com.bugvm.apple.dispatch.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 8.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("Metal") @NativeProtocolProxy/*</annotations>*/
/*<visibility>*/public final/*</visibility>*/ class /*<name>*/MTLBuffer/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements MTLResource/*</implements>*/ {

    /*<ptr>*/public static class MTLBufferPtr extends Ptr<MTLBuffer, MTLBufferPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(MTLBuffer.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "length")
    public native @MachineSizedUInt long getLength();
    @Property(selector = "label")
    public native String getLabel();
    @Property(selector = "setLabel:")
    public native void setLabel(String v);
    @Property(selector = "device")
    public native MTLDevice getDevice();
    @Property(selector = "cpuCacheMode")
    public native MTLCPUCacheMode getCpuCacheMode();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "storageMode")
    public native MTLStorageMode getStorageMode();
    /*</properties>*/
    /*<members>*//*</members>*/
    public ByteBuffer getContents() {
        return VM.newDirectByteBuffer(getContents0(), (int) getLength());
    }
    /*<methods>*/
    @Method(selector = "contents")
    protected native @Pointer long getContents0();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "newTextureWithDescriptor:offset:bytesPerRow:")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSObject.NoRetainMarshaler.class) MTLTexture newTexture(MTLTextureDescriptor descriptor, @MachineSizedUInt long offset, @MachineSizedUInt long bytesPerRow);
    @Method(selector = "setPurgeableState:")
    public native MTLPurgeableState setPurgeableState(MTLPurgeableState state);
    /*</methods>*/
}

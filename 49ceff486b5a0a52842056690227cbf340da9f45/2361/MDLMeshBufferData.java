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
/**
 * @since Available in iOS 9.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("ModelIO") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/MDLMeshBufferData/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements MDLMeshBuffer/*</implements>*/ {

    /*<ptr>*/public static class MDLMeshBufferDataPtr extends Ptr<MDLMeshBufferData, MDLMeshBufferDataPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(MDLMeshBufferData.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public MDLMeshBufferData() {}
    protected MDLMeshBufferData(SkipInit skipInit) { super(skipInit); }
    public MDLMeshBufferData(MDLMeshBufferType type, @MachineSizedUInt long length) { super((SkipInit) null); initObject(init(type, length)); }
    public MDLMeshBufferData(MDLMeshBufferType type, NSData data) { super((SkipInit) null); initObject(init(type, data)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "data")
    public native NSData getData();
    @Property(selector = "length")
    public native @MachineSizedUInt long getLength();
    @Property(selector = "allocator")
    public native MDLMeshBufferAllocator getAllocator();
    @Property(selector = "zone")
    public native MDLMeshBufferZone getZone();
    @Property(selector = "type")
    public native MDLMeshBufferType getType();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithType:length:")
    protected native @Pointer long init(MDLMeshBufferType type, @MachineSizedUInt long length);
    @Method(selector = "initWithType:data:")
    protected native @Pointer long init(MDLMeshBufferType type, NSData data);
    @Method(selector = "fillData:offset:")
    public native void fill(NSData data, @MachineSizedUInt long offset);
    @Method(selector = "map")
    public native MDLMeshBufferMap getMap();
    /*</methods>*/
}

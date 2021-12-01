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
/*<visibility>*/public final/*</visibility>*/ class /*<name>*/MTLDevice/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements NSObjectProtocol/*</implements>*/ {

    /*<ptr>*/public static class MTLDevicePtr extends Ptr<MTLDevice, MTLDevicePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(MTLDevice.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "name")
    public native String getName();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "maxThreadsPerThreadgroup")
    public native @ByVal MTLSize getMaxThreadsPerThreadgroup();
    /*</properties>*/
    /*<members>*//*</members>*/
    public MTLBuffer newBuffer(byte[] bytes, MTLResourceOptions options) {
        return newBuffer(VM.getArrayValuesAddress(bytes), bytes.length, options);
    }
    public MTLBuffer newBuffer(byte[] bytes, MTLResourceOptions options, final VoidBlock1<byte[]> deallocator) {
        return newBuffer(VM.getArrayValuesAddress(bytes), bytes.length, options, new VoidBlock2<Long, Long>() {
            @Override
            public void invoke(Long a, Long b) {
                byte[] bytes = VM.newByteArray(a, b.intValue());
                deallocator.invoke(bytes);
            }
        });
    }
    public MTLRenderPipelineState newRenderPipelineState(MTLRenderPipelineDescriptor descriptor, MTLPipelineOption options) throws NSErrorException {
        return newRenderPipelineState(descriptor, options, (MTLRenderPipelineReflection.MTLRenderPipelineReflectionPtr)null);
    }
    public MTLComputePipelineState newComputePipelineState(MTLFunction computeFunction, MTLPipelineOption options) throws NSErrorException {
        return newComputePipelineState(computeFunction, options, (MTLComputePipelineReflection.MTLComputePipelineReflectionPtr)null);
    }
    /*<methods>*/
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Bridge(symbol="MTLCreateSystemDefaultDevice", optional=true)
    public static native @com.bugvm.rt.bro.annotation.Marshaler(NSObject.NoRetainMarshaler.class) MTLDevice getSystemDefaultDevice();
    
    @Method(selector = "newCommandQueue")
    public native MTLCommandQueue newCommandQueue();
    @Method(selector = "newCommandQueueWithMaxCommandBufferCount:")
    public native MTLCommandQueue newCommandQueue(@MachineSizedUInt long maxCommandBufferCount);
    @Method(selector = "newBufferWithLength:options:")
    public native MTLBuffer newBuffer(@MachineSizedUInt long length, MTLResourceOptions options);
    @Method(selector = "newBufferWithBytes:length:options:")
    protected native MTLBuffer newBuffer(@Pointer long pointer, @MachineSizedUInt long length, MTLResourceOptions options);
    @Method(selector = "newBufferWithBytesNoCopy:length:options:deallocator:")
    protected native MTLBuffer newBuffer(@Pointer long pointer, @MachineSizedUInt long length, MTLResourceOptions options, @Block("(@Pointer,@MachineSizedUInt)") VoidBlock2<Long, Long> deallocator);
    @Method(selector = "newDepthStencilStateWithDescriptor:")
    public native MTLDepthStencilState newDepthStencilState(MTLDepthStencilDescriptor descriptor);
    @Method(selector = "newTextureWithDescriptor:")
    public native MTLTexture newTexture(MTLTextureDescriptor descriptor);
    @Method(selector = "newSamplerStateWithDescriptor:")
    public native MTLSamplerState newSamplerState(MTLSamplerDescriptor descriptor);
    @Method(selector = "newDefaultLibrary")
    public native MTLLibrary newDefaultLibrary();
    public MTLLibrary newLibraryWithFile(String filepath) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLLibrary result = newLibraryWithFile(filepath, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @Method(selector = "newLibraryWithFile:error:")
    private native MTLLibrary newLibraryWithFile(String filepath, NSError.NSErrorPtr error);
    @WeaklyLinked
    public MTLLibrary newLibraryWithData(DispatchData data) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLLibrary result = newLibraryWithData(data, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @WeaklyLinked
    @Method(selector = "newLibraryWithData:error:")
    private native MTLLibrary newLibraryWithData(DispatchData data, NSError.NSErrorPtr error);
    public MTLLibrary newLibraryWithSource(String source, MTLCompileOptions options) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLLibrary result = newLibraryWithSource(source, options, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @Method(selector = "newLibraryWithSource:options:error:")
    private native MTLLibrary newLibraryWithSource(String source, MTLCompileOptions options, NSError.NSErrorPtr error);
    @Method(selector = "newLibraryWithSource:options:completionHandler:")
    public native void newLibraryWithSource(String source, MTLCompileOptions options, @Block VoidBlock2<MTLLibrary, NSError> completionHandler);
    public MTLRenderPipelineState newRenderPipelineState(MTLRenderPipelineDescriptor descriptor) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLRenderPipelineState result = newRenderPipelineState(descriptor, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @Method(selector = "newRenderPipelineStateWithDescriptor:error:")
    private native MTLRenderPipelineState newRenderPipelineState(MTLRenderPipelineDescriptor descriptor, NSError.NSErrorPtr error);
    public MTLRenderPipelineState newRenderPipelineState(MTLRenderPipelineDescriptor descriptor, MTLPipelineOption options, MTLRenderPipelineReflection.MTLRenderPipelineReflectionPtr reflection) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLRenderPipelineState result = newRenderPipelineState(descriptor, options, reflection, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @Method(selector = "newRenderPipelineStateWithDescriptor:options:reflection:error:")
    private native MTLRenderPipelineState newRenderPipelineState(MTLRenderPipelineDescriptor descriptor, MTLPipelineOption options, MTLRenderPipelineReflection.MTLRenderPipelineReflectionPtr reflection, NSError.NSErrorPtr error);
    @Method(selector = "newRenderPipelineStateWithDescriptor:completionHandler:")
    public native void newRenderPipelineState(MTLRenderPipelineDescriptor descriptor, @Block VoidBlock2<MTLRenderPipelineState, NSError> completionHandler);
    @Method(selector = "newRenderPipelineStateWithDescriptor:options:completionHandler:")
    public native void newRenderPipelineState(MTLRenderPipelineDescriptor descriptor, MTLPipelineOption options, @Block VoidBlock3<MTLRenderPipelineState, MTLRenderPipelineReflection, NSError> completionHandler);
    public MTLComputePipelineState newComputePipelineState(MTLFunction computeFunction) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLComputePipelineState result = newComputePipelineState(computeFunction, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @Method(selector = "newComputePipelineStateWithFunction:error:")
    private native MTLComputePipelineState newComputePipelineState(MTLFunction computeFunction, NSError.NSErrorPtr error);
    public MTLComputePipelineState newComputePipelineState(MTLFunction computeFunction, MTLPipelineOption options, MTLComputePipelineReflection.MTLComputePipelineReflectionPtr reflection) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLComputePipelineState result = newComputePipelineState(computeFunction, options, reflection, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @Method(selector = "newComputePipelineStateWithFunction:options:reflection:error:")
    private native MTLComputePipelineState newComputePipelineState(MTLFunction computeFunction, MTLPipelineOption options, MTLComputePipelineReflection.MTLComputePipelineReflectionPtr reflection, NSError.NSErrorPtr error);
    @Method(selector = "newComputePipelineStateWithFunction:completionHandler:")
    public native void newComputePipelineState(MTLFunction computeFunction, @Block VoidBlock2<MTLComputePipelineState, NSError> completionHandler);
    @Method(selector = "newComputePipelineStateWithFunction:options:completionHandler:")
    public native void newComputePipelineState(MTLFunction computeFunction, MTLPipelineOption options, @Block VoidBlock3<MTLComputePipelineState, MTLComputePipelineReflection, NSError> completionHandler);
    /**
     * @since Available in iOS 9.0 and later.
     */
    public MTLComputePipelineState newComputePipelineState(MTLComputePipelineDescriptor descriptor, MTLPipelineOption options, MTLComputePipelineReflection.MTLComputePipelineReflectionPtr reflection) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       MTLComputePipelineState result = newComputePipelineState(descriptor, options, reflection, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "newComputePipelineStateWithDescriptor:options:reflection:error:")
    private native MTLComputePipelineState newComputePipelineState(MTLComputePipelineDescriptor descriptor, MTLPipelineOption options, MTLComputePipelineReflection.MTLComputePipelineReflectionPtr reflection, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "newComputePipelineStateWithDescriptor:options:completionHandler:")
    public native void newComputePipelineState(MTLComputePipelineDescriptor descriptor, MTLPipelineOption options, @Block VoidBlock3<MTLComputePipelineState, MTLComputePipelineReflection, NSError> completionHandler);
    @Method(selector = "supportsFeatureSet:")
    public native boolean supportsFeatureSet(MTLFeatureSet featureSet);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "supportsTextureSampleCount:")
    public native boolean supportsTextureSampleCount(@MachineSizedUInt long sampleCount);
    /*</methods>*/
}

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
package com.bugvm.apple.foundation;

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
import com.bugvm.apple.uikit.*;
import com.bugvm.apple.coretext.*;
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coredata.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coremedia.*;
import com.bugvm.apple.security.*;
import com.bugvm.apple.dispatch.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Library("Foundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSMutableData/*</name>*/ 
    extends /*<extends>*/NSData/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class NSMutableDataPtr extends Ptr<NSMutableData, NSMutableDataPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSMutableData.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSMutableData() {}
    protected NSMutableData(SkipInit skipInit) { super(skipInit); }
    public NSMutableData(@MachineSizedUInt long capacity) { super((SkipInit) null); initObject(init(capacity)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "mutableBytes")
    protected native @Pointer long getMutableBytes();
    /*</properties>*/
    /*<members>*//*</members>*/
    
    static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException("length=" + arrayLength + "; regionStart=" + offset
                    + "; regionLength=" + count);
        }
    }
    
    public ByteBuffer asByteBuffer() {
        return VM.newDirectByteBuffer(getMutableBytes(), getLength());
    }
    
    public NSMutableData append(ByteBuffer bytes) {
        long handle = getEffectiveAddress(bytes) + bytes.position();
        appendBytes(handle, bytes.remaining());
        return this;
    }

    public NSMutableData append(byte[] bytes) {
        return append(bytes, 0, bytes.length);
    }
    
    public NSMutableData append(byte[] bytes, int offset, int length) {
        checkOffsetAndCount(bytes.length, offset, length);
        if (length == 0) {
            return this;
        }
        appendBytes(VM.getArrayValuesAddress(bytes) + offset, length);
        return this;
    }

    public NSMutableData insert(long location, ByteBuffer bytes) {
        return replace(new NSRange(location, 0), bytes);
    }

    public NSMutableData insert(long location, byte[] bytes) {
        return replace(new NSRange(location, 0), bytes, 0, bytes.length);
    }
    
    public NSMutableData insert(long location, byte[] bytes, int offset, int length) {
        return replace(new NSRange(location, 0), bytes, offset, length);
    }

    public NSMutableData replace(NSRange range, ByteBuffer bytes) {
        long handle = getEffectiveAddress(bytes) + bytes.position();
        replaceBytes(range, handle, bytes.remaining());
        return this;
    }

    public NSMutableData replace(NSRange range, byte[] bytes) {
        return replace(range, bytes, 0, bytes.length);
    }
    
    public NSMutableData replace(NSRange range, byte[] bytes, int offset, int length) {
        checkOffsetAndCount(bytes.length, offset, length);
        replaceBytes(range, VM.getArrayValuesAddress(bytes) + offset, length);
        return this;
    }
    
    public NSMutableData delete(NSRange range) {
        replaceBytes(range, 0, 0);
        return this;
    }
    
    /*<methods>*/
    @Method(selector = "appendBytes:length:")
    protected native void appendBytes(@Pointer long bytes, @MachineSizedUInt long length);
    @Method(selector = "appendData:")
    public native void append(NSData other);
    @Method(selector = "increaseLengthBy:")
    public native void increaseLength(@MachineSizedUInt long extraLength);
    @Method(selector = "replaceBytesInRange:withBytes:")
    protected native void replaceBytes(@ByVal NSRange range, @Pointer long bytes);
    @Method(selector = "resetBytesInRange:")
    public native void reset(@ByVal NSRange range);
    @Method(selector = "setData:")
    public native void setData(NSData data);
    @Method(selector = "replaceBytesInRange:withBytes:length:")
    protected native void replaceBytes(@ByVal NSRange range, @Pointer long replacementBytes, @MachineSizedUInt long replacementLength);
    @Method(selector = "initWithCapacity:")
    protected native @Pointer long init(@MachineSizedUInt long capacity);
    /*</methods>*/
}

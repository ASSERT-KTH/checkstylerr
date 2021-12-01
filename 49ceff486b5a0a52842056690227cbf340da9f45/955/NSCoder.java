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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSCoder/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class NSCoderPtr extends Ptr<NSCoder, NSCoderPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSCoder.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSCoder() {}
    protected NSCoder(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "systemVersion")
    public native int getSystemVersion();
    @Property(selector = "allowsKeyedCoding")
    public native boolean allowsKeyedCoding();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "requiresSecureCoding")
    public native boolean requiresSecureCoding();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "allowedClasses")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsListMarshaler.class) List<ObjCClass> getAllowedClasses();
    /*</properties>*/
    /*<members>*//*</members>*/
    
    /* UIKit extensions */
    @WeaklyLinked
    public void encodeCGPoint(String key, CGPoint point) {
        com.bugvm.apple.uikit.NSCoderExtensions.encodeCGPoint(this, point, key);
    }
    @WeaklyLinked
    public void encodeCGSize(String key, CGSize size) {
        com.bugvm.apple.uikit.NSCoderExtensions.encodeCGSize(this, size, key);
    }
    @WeaklyLinked
    public void encodeCGRect(String key, CGRect rect) {
        com.bugvm.apple.uikit.NSCoderExtensions.encodeCGRect(this, rect, key);
    }
    @WeaklyLinked
    public void encodeCGAffineTransform(String key, CGAffineTransform transform) {
        com.bugvm.apple.uikit.NSCoderExtensions.encodeCGAffineTransform(this, transform, key);
    }
    @WeaklyLinked
    public void encodeUIEdgeInsets(String key, UIEdgeInsets insets) {
        com.bugvm.apple.uikit.NSCoderExtensions.encodeUIEdgeInsets(this, insets, key);
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    @WeaklyLinked
    public void encodeUIOffset(String key, UIOffset offset) {
        com.bugvm.apple.uikit.NSCoderExtensions.encodeUIOffset(this, offset, key);
    }
    @WeaklyLinked
    public CGPoint decodeCGPoint(String key) {
        return com.bugvm.apple.uikit.NSCoderExtensions.decodeCGPoint(this, key);
    }
    @WeaklyLinked
    public CGSize decodeCGSize(String key) {
        return com.bugvm.apple.uikit.NSCoderExtensions.decodeCGSize(this, key);
    }
    @WeaklyLinked
    public CGRect decodeCGRect(String key) {
        return com.bugvm.apple.uikit.NSCoderExtensions.decodeCGRect(this, key);
    }
    @WeaklyLinked
    public CGAffineTransform decodeCGAffineTransform(String key) {
        return com.bugvm.apple.uikit.NSCoderExtensions.decodeCGAffineTransform(this, key);
    }
    @WeaklyLinked
    public UIEdgeInsets decodeUIEdgeInsets(String key) {
        return com.bugvm.apple.uikit.NSCoderExtensions.decodeUIEdgeInsets(this, key);
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    @WeaklyLinked
    public UIOffset decodeUIOffset(String key) {
        return com.bugvm.apple.uikit.NSCoderExtensions.decodeUIOffset(this, key);
    }
    
    /* AVFoundation extensions */
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public void encodeCMTime(String key, CMTime time) {
        com.bugvm.apple.avfoundation.NSCoderExtensions.encodeCMTime(this, time, key);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public CMTime decodeCMTime(String key) {
        return com.bugvm.apple.avfoundation.NSCoderExtensions.decodeCMTime(this, key);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public void encodeCMTimeRange(String key, CMTimeRange timeRange) {
        com.bugvm.apple.avfoundation.NSCoderExtensions.encodeCMTimeRange(this, timeRange, key);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public CMTimeRange decodeCMTimeRange(String key) {
        return com.bugvm.apple.avfoundation.NSCoderExtensions.decodeCMTimeRange(this, key);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public void encodeCMTimeMapping(String key, CMTimeMapping timeMapping) {
        com.bugvm.apple.avfoundation.NSCoderExtensions.encodeCMTimeMapping(this, timeMapping, key);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public CMTimeMapping decodeCMTimeMapping(String key) {
        return com.bugvm.apple.avfoundation.NSCoderExtensions.decodeCMTimeMapping(this, key);
    }
    
    public void encodeBytes(byte[] bytes) {
        encodeBytes0(VM.getArrayValuesAddress(bytes), bytes.length);
    }
    public byte[] decodeBytes() {
        MachineSizedUIntPtr lengthPtr = new MachineSizedUIntPtr();
        BytePtr bytePtr = decodeBytes0(lengthPtr);
        return bytePtr.toByteArray((int)lengthPtr.get());
    }
    public void encodeObject(String key, NSObject value) {
        encodeObject0(value, key);
    }
    public void encodeConditionalObject(String key, NSObject value) {
        encodeConditionalObject0(value, key);
    }
    public void encodeBoolean(String key, boolean value) {
        encodeBool0(value, key);
    }
    public void encodeInteger(String key, int value) {
        encodeInt320(value, key);
    }
    public void encodeLong(String key, long value) {
        encodeInt640(value, key);
    }
    public void encodeFloat(String key, float value) {
        encodeFloat0(value, key);
    }
    public void encodeDouble(String key, double value) {
        encodeDouble0(value, key);
    }
    public void encodeBytes(String key, byte[] bytes) {
        encodeBytes0(VM.getArrayValuesAddress(bytes), bytes.length, key);
    }
    public NSObject decodeObject(String key) {
        return decodeObject0(key);
    }
    public boolean decodeBoolean(String key) {
        return decodeBool0(key);
    }
    public int decodeInteger(String key) {
        return decodeInt320(key);
    }
    public long decodeLong(String key) {
        return decodeInt640(key);
    }
    public float decodeFloat(String key) {
        return decodeFloat0(key);
    }
    public double decodeDouble(String key) {
        return decodeDouble0(key);
    }
    public byte[] decodeBytes(String key) {
        MachineSizedUIntPtr lengthPtr = new MachineSizedUIntPtr();
        BytePtr bytesPtr = decodeBytes0(key, lengthPtr);
        return bytesPtr.toByteArray((int)lengthPtr.get());
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    public NSObject decodeObject(String key, Class<? extends NSObject> clazz) {
        return decodeObject0(clazz, key);
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    public NSObject decodeObject(String key, List<ObjCClass> clazzes) {
        return decodeObject0(clazzes, key);
    }
    
    public void encodeString(String key, String value) {
        encodeObject(key, value == null ? null : new NSString(value));
    }
    public String decodeString(String key) {
        NSObject value = decodeObject(key);
        if (value instanceof NSString) {
            return ((NSString)value).toString();
        }
        return null;
    }
    /*<methods>*/
    @Method(selector = "encodeDataObject:")
    public native void encodeDataObject(NSData data);
    @Method(selector = "decodeDataObject")
    public native NSData decodeDataObject();
    @Method(selector = "versionForClassName:")
    public native @MachineSizedSInt long getVersionForClassName(String className);
    @Method(selector = "encodeObject:")
    public native void encodeObject(NSObject object);
    @Method(selector = "encodeRootObject:")
    public native void encodeRootObject(NSObject rootObject);
    @Method(selector = "encodeBycopyObject:")
    public native void encodeBycopyObject(NSObject anObject);
    @Method(selector = "encodeByrefObject:")
    public native void encodeByrefObject(NSObject anObject);
    @Method(selector = "encodeConditionalObject:")
    public native void encodeConditionalObject(NSObject object);
    @Method(selector = "encodeBytes:length:")
    protected native void encodeBytes0(@Pointer long byteaddr, @MachineSizedUInt long length);
    @Method(selector = "decodeObject")
    public native NSObject decodeObject();
    /**
     * @since Available in iOS 9.0 and later.
     */
    public NSObject decodeTopLevelObject() throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSObject result = decodeTopLevelObject(ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "decodeTopLevelObjectAndReturnError:")
    private native NSObject decodeTopLevelObject(NSError.NSErrorPtr error);
    @Method(selector = "decodeBytesWithReturnedLength:")
    protected native BytePtr decodeBytes0(MachineSizedUIntPtr lengthp);
    @Method(selector = "setObjectZone:")
    public native void setObjectZone(NSZone zone);
    @Method(selector = "objectZone")
    public native NSZone getObjectZone();
    @Method(selector = "encodeObject:forKey:")
    protected native void encodeObject0(NSObject objv, String key);
    @Method(selector = "encodeConditionalObject:forKey:")
    protected native void encodeConditionalObject0(NSObject objv, String key);
    @Method(selector = "encodeBool:forKey:")
    protected native void encodeBool0(boolean boolv, String key);
    @Method(selector = "encodeInt:forKey:")
    protected native void encodeInt0(int intv, String key);
    @Method(selector = "encodeInt32:forKey:")
    protected native void encodeInt320(int intv, String key);
    @Method(selector = "encodeInt64:forKey:")
    protected native void encodeInt640(long intv, String key);
    @Method(selector = "encodeFloat:forKey:")
    protected native void encodeFloat0(float realv, String key);
    @Method(selector = "encodeDouble:forKey:")
    protected native void encodeDouble0(double realv, String key);
    @Method(selector = "encodeBytes:length:forKey:")
    protected native void encodeBytes0(@Pointer long bytesp, @MachineSizedUInt long lenv, String key);
    @Method(selector = "containsValueForKey:")
    public native boolean containsValue(String key);
    @Method(selector = "decodeObjectForKey:")
    protected native NSObject decodeObject0(String key);
    /**
     * @since Available in iOS 9.0 and later.
     */
    public NSObject decodeTopLevelObject(String key) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSObject result = decodeTopLevelObject(key, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "decodeTopLevelObjectForKey:error:")
    private native NSObject decodeTopLevelObject(String key, NSError.NSErrorPtr error);
    @Method(selector = "decodeBoolForKey:")
    protected native boolean decodeBool0(String key);
    @Method(selector = "decodeIntForKey:")
    protected native int decodeInt0(String key);
    @Method(selector = "decodeInt32ForKey:")
    protected native int decodeInt320(String key);
    @Method(selector = "decodeInt64ForKey:")
    protected native long decodeInt640(String key);
    @Method(selector = "decodeFloatForKey:")
    protected native float decodeFloat0(String key);
    @Method(selector = "decodeDoubleForKey:")
    protected native double decodeDouble0(String key);
    @Method(selector = "decodeBytesForKey:returnedLength:")
    protected native BytePtr decodeBytes0(String key, MachineSizedUIntPtr lengthp);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Method(selector = "encodeInteger:forKey:")
    protected native void encodeInteger0(@MachineSizedSInt long intv, String key);
    /**
     * @since Available in iOS 2.0 and later.
     */
    @Method(selector = "decodeIntegerForKey:")
    protected native @MachineSizedSInt long decodeInteger0(String key);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "decodeObjectOfClass:forKey:")
    protected native NSObject decodeObject0(Class<? extends NSObject> aClass, String key);
    /**
     * @since Available in iOS 9.0 and later.
     */
    public NSObject decodeTopLevelObject(Class<? extends NSObject> aClass, String key) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSObject result = decodeTopLevelObject(aClass, key, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "decodeTopLevelObjectOfClass:forKey:error:")
    private native NSObject decodeTopLevelObject(Class<? extends NSObject> aClass, String key, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "decodeObjectOfClasses:forKey:")
    protected native NSObject decodeObject0(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsListMarshaler.class) List<ObjCClass> classes, String key);
    /**
     * @since Available in iOS 9.0 and later.
     */
    public NSObject decodeTopLevelObject(NSSet<?> classes, String key) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSObject result = decodeTopLevelObject(classes, key, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "decodeTopLevelObjectOfClasses:forKey:error:")
    private native NSObject decodeTopLevelObject(NSSet<?> classes, String key, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "decodePropertyListForKey:")
    protected native NSObject decodePropertyList0(String key);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "failWithError:")
    public native void fail(NSError error);
    /*</methods>*/
}

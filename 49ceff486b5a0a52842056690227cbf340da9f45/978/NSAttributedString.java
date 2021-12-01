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
/**
 * @since Available in iOS 3.2 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("Foundation") @NativeClass @WeaklyLinked/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSAttributedString/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class NSAttributedStringPtr extends Ptr<NSAttributedString, NSAttributedStringPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSAttributedString.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSAttributedString() {}
    protected NSAttributedString(SkipInit skipInit) { super(skipInit); }
    public NSAttributedString(String str) { super((SkipInit) null); initObject(init(str)); }
    public NSAttributedString(String str, NSDictionary<NSString, ?> attrs) { super((SkipInit) null); initObject(init(str, attrs)); }
    public NSAttributedString(NSAttributedString attrStr) { super((SkipInit) null); initObject(init(attrStr)); }
    /*</constructors>*/
    public NSAttributedString(String str, NSAttributedStringAttributes attrs) {
        super((SkipInit)null);
        if (attrs == null) {
            throw new NullPointerException("attrs");
        }
        initObject(init(str, attrs.getDictionary()));
    }
    public NSAttributedString(String str, CMTextMarkupAttributes attrs) {
        super((SkipInit)null);
        if (attrs == null) {
            throw new NullPointerException("attrs");
        }
        initObject(init(str, attrs.getDictionary().as(NSDictionary.class)));
    }
    public NSAttributedString(String str, CTAttributedStringAttributes attrs) {
        super((SkipInit)null);
        if (attrs == null) {
            throw new NullPointerException("attrs");
        }
        initObject(init(str, attrs.getDictionary().as(NSDictionary.class)));
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @WeaklyLinked
    public NSAttributedString(NSTextAttachment attachment) {
        super(NSAttributedStringExtensions.create(attachment));
        retain(getHandle());
    }
    /*<properties>*/
    @Property(selector = "string")
    public native String getString();
    @Property(selector = "length")
    public native @MachineSizedUInt long length();
    /*</properties>*/
    /*<members>*//*</members>*/
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NSAttributedString)) {
            return false;
        }
        return equalsTo((NSAttributedString) obj);
    }
    @WeaklyLinked
    public NSObject getAttribute(String name, @MachineSizedUInt long location, NSRange range) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return getAttribute(new NSString(name), location, range);
    }
    @WeaklyLinked
    public NSObject getAttribute(NSAttributedStringAttribute attribute, @MachineSizedUInt long location, NSRange range) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        return getAttribute(attribute.value(), location, range);
    }
    @WeaklyLinked
    public NSObject getAttribute(CMTextMarkupAttribute attribute, @MachineSizedUInt long location, NSRange range) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        return getAttribute(attribute.value().as(NSString.class), location, range);
    }
    @WeaklyLinked
    public NSObject getAttribute(CTAttributedStringAttribute attribute, @MachineSizedUInt long location, NSRange range) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        return getAttribute(attribute.value().as(NSString.class), location, range);
    }
    @WeaklyLinked
    public NSObject getAttribute(String name, @MachineSizedUInt long location, NSRange range, @ByVal NSRange rangeLimit) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return getAttribute(new NSString(name), location, range, rangeLimit);
    }
    @WeaklyLinked
    public NSObject getAttribute(NSAttributedStringAttribute attribute, @MachineSizedUInt long location, NSRange range, @ByVal NSRange rangeLimit) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        return getAttribute(attribute.value(), location, range, rangeLimit);
    }
    @WeaklyLinked
    public NSObject getAttribute(CMTextMarkupAttribute attribute, @MachineSizedUInt long location, NSRange range, @ByVal NSRange rangeLimit) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        return getAttribute(attribute.value().as(NSString.class), location, range, rangeLimit);
    }
    @WeaklyLinked
    public NSObject getAttribute(CTAttributedStringAttribute attribute, @MachineSizedUInt long location, NSRange range, @ByVal NSRange rangeLimit) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        return getAttribute(attribute.value().as(NSString.class), location, range, rangeLimit);
    }
    @WeaklyLinked
    public NSAttributedStringAttributes getAttributes(long location, NSRange range) {
        return new NSAttributedStringAttributes(getAttributesDictionary(location, range));
    }
    @WeaklyLinked
    public NSAttributedStringAttributes getAttributes(long location, NSRange range, NSRange rangeLimit) {
        return new NSAttributedStringAttributes(getAttributesDictionary(location, range, rangeLimit));
    }
    @WeaklyLinked
    public CMTextMarkupAttributes getTextMarkupAttributes(long location, NSRange range) {
        return new CMTextMarkupAttributes(getAttributesDictionary(location, range).as(CFDictionary.class));
    }
    @WeaklyLinked
    public CMTextMarkupAttributes getTextMarkupAttributes(long location, NSRange range, NSRange rangeLimit) {
        return new CMTextMarkupAttributes(getAttributesDictionary(location, range, rangeLimit).as(CFDictionary.class));
    }
    @WeaklyLinked
    public CTAttributedStringAttributes getCoreTextAttributes(long location, NSRange range) {
        return new CTAttributedStringAttributes(getAttributesDictionary(location, range).as(CFDictionary.class));
    }
    @WeaklyLinked
    public CTAttributedStringAttributes getCoreTextAttributes(long location, NSRange range, NSRange rangeLimit) {
        return new CTAttributedStringAttributes(getAttributesDictionary(location, range, rangeLimit).as(CFDictionary.class));
    }
    
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public void enumerateAttribute(String name, @ByVal NSRange enumerationRange, NSAttributedStringEnumerationOptions opts, final VoidBlock3<NSObject, NSRange, BooleanPtr> block) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        enumerateAttribute(new NSString(name), enumerationRange, opts, block);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public void enumerateAttribute(NSAttributedStringAttribute attribute, @ByVal NSRange enumerationRange, NSAttributedStringEnumerationOptions opts, final VoidBlock3<NSObject, NSRange, BooleanPtr> block) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        enumerateAttribute(attribute.value(), enumerationRange, opts, block);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public void enumerateAttribute(CMTextMarkupAttribute attribute, @ByVal NSRange enumerationRange, NSAttributedStringEnumerationOptions opts, final VoidBlock3<NSObject, NSRange, BooleanPtr> block) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        enumerateAttribute(attribute.value().as(NSString.class), enumerationRange, opts, block);
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @WeaklyLinked
    public void enumerateAttribute(CTAttributedStringAttribute attribute, @ByVal NSRange enumerationRange, NSAttributedStringEnumerationOptions opts, final VoidBlock3<NSObject, NSRange, BooleanPtr> block) {
        if (attribute == null) {
            throw new NullPointerException("attribute");
        }
        enumerateAttribute(attribute.value().as(NSString.class), enumerationRange, opts, block);
    }
    
    /* UIKit extensions */
    /**
     * 
     * @param url
     * @param options
     * @return
     * @since Available in iOS 7.0 and later.
     * @throws NSErrorException
     */
    @WeaklyLinked
    public NSAttributedString(NSURL url, NSAttributedStringDocumentAttributes options) throws NSErrorException {
        super((SkipInit) null);
        long h = NSObject.alloc(ObjCClass.getByType(NSAttributedString.class));
        initObject(NSAttributedStringExtensions.init(ObjCObject.toObjCObject(NSAttributedString.class, h, NSObject.FLAG_NO_RETAIN), url, options, null));
    }
    /**
     * 
     * @param data
     * @param options
     * @return
     * @since Available in iOS 7.0 and later.
     * @throws NSErrorException
     */
    @WeaklyLinked
    public NSAttributedString(NSData data, NSAttributedStringDocumentAttributes options) throws NSErrorException {
        super((SkipInit) null);
        long h = NSObject.alloc(ObjCClass.getByType(NSAttributedString.class));
        initObject(NSAttributedStringExtensions.init(ObjCObject.toObjCObject(NSAttributedString.class, h, NSObject.FLAG_NO_RETAIN), data, options, null));
    }
    /**
     * 
     * @param range
     * @param dict
     * @return
     * @since Available in iOS 7.0 and later.
     * @throws NSErrorException
     */
    @WeaklyLinked
    public NSData getData(NSRange range, NSAttributedStringDocumentAttributes dict) throws NSErrorException {
        return NSAttributedStringExtensions.getData(this, range, dict);
    }
    /**
     * 
     * @param range
     * @param dict
     * @return
     * @since Available in iOS 7.0 and later.
     * @throws NSErrorException
     */
    @WeaklyLinked
    public NSFileWrapper getFileWrapper(NSRange range, NSAttributedStringDocumentAttributes dict) throws NSErrorException {
        return NSAttributedStringExtensions.getFileWrapper(this, range, dict);
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @WeaklyLinked
    public CGSize getSize() {
        return NSAttributedStringExtensions.getSize(this);
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @WeaklyLinked
    public void draw(CGPoint point) {
        NSAttributedStringExtensions.draw(this, point);
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @WeaklyLinked
    public void draw(CGRect rect) {
        NSAttributedStringExtensions.draw(this, rect);
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @WeaklyLinked
    public void draw(@ByVal CGRect rect, NSStringDrawingOptions options, NSStringDrawingContext context) {
        NSAttributedStringExtensions.draw(this, rect, options, context);
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @WeaklyLinked
    public CGRect getBoundingRect(@ByVal CGSize size, NSStringDrawingOptions options, NSStringDrawingContext context) {
        return NSAttributedStringExtensions.getBoundingRect(this, size, options, context);
    }
    /*<methods>*/
    @Method(selector = "attributesAtIndex:effectiveRange:")
    public native NSDictionary<NSString, ?> getAttributesDictionary(@MachineSizedUInt long location, NSRange range);
    @Method(selector = "attribute:atIndex:effectiveRange:")
    public native NSObject getAttribute(NSString attrName, @MachineSizedUInt long location, NSRange range);
    @Method(selector = "attributedSubstringFromRange:")
    public native NSAttributedString substring(@ByVal NSRange range);
    @Method(selector = "attributesAtIndex:longestEffectiveRange:inRange:")
    public native NSDictionary<NSString, ?> getAttributesDictionary(@MachineSizedUInt long location, NSRange range, @ByVal NSRange rangeLimit);
    @Method(selector = "attribute:atIndex:longestEffectiveRange:inRange:")
    public native NSObject getAttribute(NSString attrName, @MachineSizedUInt long location, NSRange range, @ByVal NSRange rangeLimit);
    @Method(selector = "isEqualToAttributedString:")
    public native boolean equalsTo(NSAttributedString other);
    @Method(selector = "initWithString:")
    protected native @Pointer long init(String str);
    @Method(selector = "initWithString:attributes:")
    protected native @Pointer long init(String str, NSDictionary<NSString, ?> attrs);
    @Method(selector = "initWithAttributedString:")
    protected native @Pointer long init(NSAttributedString attrStr);
    /**
     * @since Available in iOS 4.0 and later.
     */
    @Method(selector = "enumerateAttributesInRange:options:usingBlock:")
    public native void enumerateAttributes(@ByVal NSRange enumerationRange, NSAttributedStringEnumerationOptions opts, @Block("(,@ByVal,)") VoidBlock3<NSDictionary<NSString, ?>, NSRange, BooleanPtr> block);
    /**
     * @since Available in iOS 4.0 and later.
     */
    @Method(selector = "enumerateAttribute:inRange:options:usingBlock:")
    public native void enumerateAttribute(NSString attrName, @ByVal NSRange enumerationRange, NSAttributedStringEnumerationOptions opts, @Block("(,@ByVal,)") VoidBlock3<NSObject, NSRange, BooleanPtr> block);
    /*</methods>*/
}

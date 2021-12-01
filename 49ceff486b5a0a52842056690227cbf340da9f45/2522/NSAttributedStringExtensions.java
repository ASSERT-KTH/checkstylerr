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
package com.bugvm.apple.uikit;

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
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coredata.*;
import com.bugvm.apple.coreimage.*;
import com.bugvm.apple.coretext.*;
import com.bugvm.apple.corelocation.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("UIKit")/*</annotations>*/
/*<visibility>*/public final/*</visibility>*/ class /*<name>*/NSAttributedStringExtensions/*</name>*/ 
    extends /*<extends>*/NSExtensions/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSAttributedStringExtensions.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    private NSAttributedStringExtensions() {}
    /*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static @Pointer long init(NSAttributedString thiz, NSURL url, NSAttributedStringDocumentAttributes options, NSDictionary.NSDictionaryPtr dict) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       long result = init(thiz, url, options, dict, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "initWithURL:options:documentAttributes:error:")
    private static native @Pointer long init(NSAttributedString thiz, NSURL url, NSAttributedStringDocumentAttributes options, NSDictionary.NSDictionaryPtr dict, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static @Pointer long init(NSAttributedString thiz, NSData data, NSAttributedStringDocumentAttributes options, NSDictionary.NSDictionaryPtr dict) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       long result = init(thiz, data, options, dict, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "initWithData:options:documentAttributes:error:")
    private static native @Pointer long init(NSAttributedString thiz, NSData data, NSAttributedStringDocumentAttributes options, NSDictionary.NSDictionaryPtr dict, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static NSData getData(NSAttributedString thiz, @ByVal NSRange range, NSAttributedStringDocumentAttributes dict) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSData result = getData(thiz, range, dict, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "dataFromRange:documentAttributes:error:")
    private static native NSData getData(NSAttributedString thiz, @ByVal NSRange range, NSAttributedStringDocumentAttributes dict, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static NSFileWrapper getFileWrapper(NSAttributedString thiz, @ByVal NSRange range, NSAttributedStringDocumentAttributes dict) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSFileWrapper result = getFileWrapper(thiz, range, dict, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "fileWrapperFromRange:documentAttributes:error:")
    private static native NSFileWrapper getFileWrapper(NSAttributedString thiz, @ByVal NSRange range, NSAttributedStringDocumentAttributes dict, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "containsAttachmentsInRange:")
    public static native boolean containsAttachments(NSAttributedString thiz, @ByVal NSRange range);
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    public static @Pointer long initWithFileURL(NSAttributedString thiz, NSURL url, NSAttributedStringDocumentAttributes options, NSDictionary.NSDictionaryPtr dict) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       long result = initWithFileURL(thiz, url, options, dict, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Method(selector = "initWithFileURL:options:documentAttributes:error:")
    private static native @Pointer long initWithFileURL(NSAttributedString thiz, NSURL url, NSAttributedStringDocumentAttributes options, NSDictionary.NSDictionaryPtr dict, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "size")
    public static native @ByVal CGSize getSize(NSAttributedString thiz);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "drawAtPoint:")
    public static native void draw(NSAttributedString thiz, @ByVal CGPoint point);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "drawInRect:")
    public static native void draw(NSAttributedString thiz, @ByVal CGRect rect);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "drawWithRect:options:context:")
    public static native void draw(NSAttributedString thiz, @ByVal CGRect rect, NSStringDrawingOptions options, NSStringDrawingContext context);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "boundingRectWithSize:options:context:")
    public static native @ByVal CGRect getBoundingRect(NSAttributedString thiz, @ByVal CGSize size, NSStringDrawingOptions options, NSStringDrawingContext context);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "attributedStringWithAttachment:")
    protected static native @Pointer long create(ObjCClass clazz, NSTextAttachment attachment);
    public static @Pointer long create(NSTextAttachment attachment) { return create(ObjCClass.getByType(NSAttributedString.class), attachment); }
    /*</methods>*/
}

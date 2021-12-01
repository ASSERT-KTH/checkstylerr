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
/*<annotations>*/@Library("UIKit") @StronglyLinked/*</annotations>*/
@Marshaler(/*<name>*/NSAttributedStringDocumentAttribute/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSAttributedStringDocumentAttribute/*</name>*/ 
    extends /*<extends>*/GlobalValueEnumeration<NSString>/*</extends>*/
    /*<implements>*//*</implements>*/ {

    static { Bro.bind(/*<name>*/NSAttributedStringDocumentAttribute/*</name>*/.class); }

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static NSAttributedStringDocumentAttribute toObject(Class<NSAttributedStringDocumentAttribute> cls, long handle, long flags) {
            NSString o = (NSString) NSObject.Marshaler.toObject(NSString.class, handle, flags);
            if (o == null) {
                return null;
            }
            return NSAttributedStringDocumentAttribute.valueOf(o);
        }
        @MarshalsPointer
        public static long toNative(NSAttributedStringDocumentAttribute o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.value(), flags);
        }
    }
    public static class AsListMarshaler {
        @SuppressWarnings("unchecked")
        @MarshalsPointer
        public static List<NSAttributedStringDocumentAttribute> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSString> o = (NSArray<NSString>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<NSAttributedStringDocumentAttribute> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(NSAttributedStringDocumentAttribute.valueOf(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<NSAttributedStringDocumentAttribute> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSString> array = new NSMutableArray<>();
            for (NSAttributedStringDocumentAttribute o : l) {
                array.add(o.value());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constants>*/
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute DocumentType = new NSAttributedStringDocumentAttribute("DocumentType");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute CharacterEncoding = new NSAttributedStringDocumentAttribute("CharacterEncoding");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute DefaultAttributes = new NSAttributedStringDocumentAttribute("DefaultAttributes");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute PaperSize = new NSAttributedStringDocumentAttribute("PaperSize");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute PaperMargin = new NSAttributedStringDocumentAttribute("PaperMargin");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute ViewSize = new NSAttributedStringDocumentAttribute("ViewSize");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute ViewZoom = new NSAttributedStringDocumentAttribute("ViewZoom");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute ViewMode = new NSAttributedStringDocumentAttribute("ViewMode");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute ReadOnly = new NSAttributedStringDocumentAttribute("ReadOnly");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute BackgroundColor = new NSAttributedStringDocumentAttribute("BackgroundColor");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute HyphenationFactor = new NSAttributedStringDocumentAttribute("HyphenationFactor");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute DefaultTabInterval = new NSAttributedStringDocumentAttribute("DefaultTabInterval");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSAttributedStringDocumentAttribute TextLayoutSections = new NSAttributedStringDocumentAttribute("TextLayoutSections");
    /*</constants>*/
    
    private static /*<name>*/NSAttributedStringDocumentAttribute/*</name>*/[] values = new /*<name>*/NSAttributedStringDocumentAttribute/*</name>*/[] {/*<value_list>*/DocumentType, CharacterEncoding, DefaultAttributes, PaperSize, PaperMargin, ViewSize, ViewZoom, ViewMode, ReadOnly, BackgroundColor, HyphenationFactor, DefaultTabInterval, TextLayoutSections/*</value_list>*/};
    
    /*<name>*/NSAttributedStringDocumentAttribute/*</name>*/ (String getterName) {
        super(Values.class, getterName);
    }
    
    public static /*<name>*/NSAttributedStringDocumentAttribute/*</name>*/ valueOf(/*<type>*/NSString/*</type>*/ value) {
        for (/*<name>*/NSAttributedStringDocumentAttribute/*</name>*/ v : values) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + value + " found in " 
            + /*<name>*/NSAttributedStringDocumentAttribute/*</name>*/.class.getName());
    }
    
    /*<methods>*//*</methods>*/
    
    /*<annotations>*/@Library("UIKit") @StronglyLinked/*</annotations>*/
    public static class Values {
    	static { Bro.bind(Values.class); }

        /*<values>*/
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSDocumentTypeDocumentAttribute", optional=true)
        public static native NSString DocumentType();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSCharacterEncodingDocumentAttribute", optional=true)
        public static native NSString CharacterEncoding();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSDefaultAttributesDocumentAttribute", optional=true)
        public static native NSString DefaultAttributes();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSPaperSizeDocumentAttribute", optional=true)
        public static native NSString PaperSize();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSPaperMarginDocumentAttribute", optional=true)
        public static native NSString PaperMargin();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSViewSizeDocumentAttribute", optional=true)
        public static native NSString ViewSize();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSViewZoomDocumentAttribute", optional=true)
        public static native NSString ViewZoom();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSViewModeDocumentAttribute", optional=true)
        public static native NSString ViewMode();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSReadOnlyDocumentAttribute", optional=true)
        public static native NSString ReadOnly();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSBackgroundColorDocumentAttribute", optional=true)
        public static native NSString BackgroundColor();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSHyphenationFactorDocumentAttribute", optional=true)
        public static native NSString HyphenationFactor();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSDefaultTabIntervalDocumentAttribute", optional=true)
        public static native NSString DefaultTabInterval();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSTextLayoutSectionsAttribute", optional=true)
        public static native NSString TextLayoutSections();
        /*</values>*/
    }
}

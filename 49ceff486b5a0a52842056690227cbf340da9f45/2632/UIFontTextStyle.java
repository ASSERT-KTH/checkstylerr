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
@Marshaler(/*<name>*/UIFontTextStyle/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIFontTextStyle/*</name>*/ 
    extends /*<extends>*/GlobalValueEnumeration<NSString>/*</extends>*/
    /*<implements>*//*</implements>*/ {

    static { Bro.bind(/*<name>*/UIFontTextStyle/*</name>*/.class); }

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static UIFontTextStyle toObject(Class<UIFontTextStyle> cls, long handle, long flags) {
            NSString o = (NSString) NSObject.Marshaler.toObject(NSString.class, handle, flags);
            if (o == null) {
                return null;
            }
            return UIFontTextStyle.valueOf(o);
        }
        @MarshalsPointer
        public static long toNative(UIFontTextStyle o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.value(), flags);
        }
    }
    public static class AsListMarshaler {
        @SuppressWarnings("unchecked")
        @MarshalsPointer
        public static List<UIFontTextStyle> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSString> o = (NSArray<NSString>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<UIFontTextStyle> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(UIFontTextStyle.valueOf(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<UIFontTextStyle> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSString> array = new NSMutableArray<>();
            for (UIFontTextStyle o : l) {
                array.add(o.value());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constants>*/
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final UIFontTextStyle Title1 = new UIFontTextStyle("Title1");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final UIFontTextStyle Title2 = new UIFontTextStyle("Title2");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final UIFontTextStyle Title3 = new UIFontTextStyle("Title3");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final UIFontTextStyle Headline = new UIFontTextStyle("Headline");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final UIFontTextStyle Subheadline = new UIFontTextStyle("Subheadline");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final UIFontTextStyle Body = new UIFontTextStyle("Body");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final UIFontTextStyle Callout = new UIFontTextStyle("Callout");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final UIFontTextStyle Footnote = new UIFontTextStyle("Footnote");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final UIFontTextStyle Caption1 = new UIFontTextStyle("Caption1");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final UIFontTextStyle Caption2 = new UIFontTextStyle("Caption2");
    /*</constants>*/
    
    private static /*<name>*/UIFontTextStyle/*</name>*/[] values = new /*<name>*/UIFontTextStyle/*</name>*/[] {/*<value_list>*/Title1, Title2, Title3, Headline, Subheadline, Body, Callout, Footnote, Caption1, Caption2/*</value_list>*/};
    
    /*<name>*/UIFontTextStyle/*</name>*/ (String getterName) {
        super(Values.class, getterName);
    }
    
    public static /*<name>*/UIFontTextStyle/*</name>*/ valueOf(/*<type>*/NSString/*</type>*/ value) {
        for (/*<name>*/UIFontTextStyle/*</name>*/ v : values) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + value + " found in " 
            + /*<name>*/UIFontTextStyle/*</name>*/.class.getName());
    }
    
    /*<methods>*//*</methods>*/
    
    /*<annotations>*/@Library("UIKit") @StronglyLinked/*</annotations>*/
    public static class Values {
    	static { Bro.bind(Values.class); }

        /*<values>*/
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleTitle1", optional=true)
        public static native NSString Title1();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleTitle2", optional=true)
        public static native NSString Title2();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleTitle3", optional=true)
        public static native NSString Title3();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleHeadline", optional=true)
        public static native NSString Headline();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleSubheadline", optional=true)
        public static native NSString Subheadline();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleBody", optional=true)
        public static native NSString Body();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleCallout", optional=true)
        public static native NSString Callout();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleFootnote", optional=true)
        public static native NSString Footnote();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleCaption1", optional=true)
        public static native NSString Caption1();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontTextStyleCaption2", optional=true)
        public static native NSString Caption2();
        /*</values>*/
    }
}

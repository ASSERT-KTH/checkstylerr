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
@Marshaler(/*<name>*/UITextInputTextStyle/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UITextInputTextStyle/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static UITextInputTextStyle toObject(Class<UITextInputTextStyle> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new UITextInputTextStyle(o);
        }
        @MarshalsPointer
        public static long toNative(UITextInputTextStyle o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<UITextInputTextStyle> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<UITextInputTextStyle> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new UITextInputTextStyle(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<UITextInputTextStyle> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (UITextInputTextStyle i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    UITextInputTextStyle(NSDictionary data) {
        super(data);
    }
    public UITextInputTextStyle() {}
    /*</constructors>*/

    /*<methods>*/
    public boolean has(NSString key) {
        return data.containsKey(key);
    }
    public NSObject get(NSString key) {
        if (has(key)) {
            return data.get(key);
        }
        return null;
    }
    public UITextInputTextStyle set(NSString key, NSObject value) {
        data.put(key, value);
        return this;
    }
    

    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public UIColor getBackgroundColor() {
        if (has(Keys.BackgroundColor())) {
            UIColor val = (UIColor) get(Keys.BackgroundColor());
            return val;
        }
        return null;
    }
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public UITextInputTextStyle setBackgroundColor(UIColor backgroundColor) {
        set(Keys.BackgroundColor(), backgroundColor);
        return this;
    }
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public UIColor getTextColor() {
        if (has(Keys.Color())) {
            UIColor val = (UIColor) get(Keys.Color());
            return val;
        }
        return null;
    }
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public UITextInputTextStyle setTextColor(UIColor textColor) {
        set(Keys.Color(), textColor);
        return this;
    }
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public UIFont getFont() {
        if (has(Keys.Font())) {
            UIFont val = (UIFont) get(Keys.Font());
            return val;
        }
        return null;
    }
    /**
     * @since Available in iOS 3.2 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public UITextInputTextStyle setFont(UIFont font) {
        set(Keys.Font(), font);
        return this;
    }
    /*</methods>*/
    
    /*<keys>*/
    @Library("UIKit")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        /**
         * @since Available in iOS 3.2 and later.
         * @deprecated Deprecated in iOS 8.0.
         */
        @Deprecated
        @GlobalValue(symbol="UITextInputTextBackgroundColorKey", optional=true)
        public static native NSString BackgroundColor();
        /**
         * @since Available in iOS 3.2 and later.
         * @deprecated Deprecated in iOS 8.0.
         */
        @Deprecated
        @GlobalValue(symbol="UITextInputTextColorKey", optional=true)
        public static native NSString Color();
        /**
         * @since Available in iOS 3.2 and later.
         * @deprecated Deprecated in iOS 8.0.
         */
        @Deprecated
        @GlobalValue(symbol="UITextInputTextFontKey", optional=true)
        public static native NSString Font();
    }
    /*</keys>*/
}

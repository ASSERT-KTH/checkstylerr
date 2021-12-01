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
@Marshaler(/*<name>*/UIFontDescriptorTraits/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIFontDescriptorTraits/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static UIFontDescriptorTraits toObject(Class<UIFontDescriptorTraits> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new UIFontDescriptorTraits(o);
        }
        @MarshalsPointer
        public static long toNative(UIFontDescriptorTraits o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<UIFontDescriptorTraits> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<UIFontDescriptorTraits> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new UIFontDescriptorTraits(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<UIFontDescriptorTraits> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (UIFontDescriptorTraits i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    UIFontDescriptorTraits(NSDictionary data) {
        super(data);
    }
    public UIFontDescriptorTraits() {}
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
    public UIFontDescriptorTraits set(NSString key, NSObject value) {
        data.put(key, value);
        return this;
    }
    

    /**
     * @since Available in iOS 7.0 and later.
     */
    public UIFontDescriptorSymbolicTraits getSymbolicTraits() {
        if (has(Keys.SymbolicTrait())) {
            NSNumber val = (NSNumber) get(Keys.SymbolicTrait());
            return new UIFontDescriptorSymbolicTraits(val.longValue());
        }
        return null;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public UIFontDescriptorTraits setSymbolicTraits(UIFontDescriptorSymbolicTraits symbolicTraits) {
        set(Keys.SymbolicTrait(), NSNumber.valueOf(symbolicTraits.value()));
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public double getWeightTrait() {
        if (has(Keys.WeightTrait())) {
            NSNumber val = (NSNumber) get(Keys.WeightTrait());
            return val.doubleValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public UIFontDescriptorTraits setWeightTrait(double weightTrait) {
        set(Keys.WeightTrait(), NSNumber.valueOf(weightTrait));
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public double getWidthTrait() {
        if (has(Keys.WidthTrait())) {
            NSNumber val = (NSNumber) get(Keys.WidthTrait());
            return val.doubleValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public UIFontDescriptorTraits setWidthTrait(double widthTrait) {
        set(Keys.WidthTrait(), NSNumber.valueOf(widthTrait));
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public double getSlantTrait() {
        if (has(Keys.SlantTrait())) {
            NSNumber val = (NSNumber) get(Keys.SlantTrait());
            return val.doubleValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public UIFontDescriptorTraits setSlantTrait(double slantTrait) {
        set(Keys.SlantTrait(), NSNumber.valueOf(slantTrait));
        return this;
    }
    /*</methods>*/
    
    /*<keys>*/
    @Library("UIKit")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontSymbolicTrait", optional=true)
        public static native NSString SymbolicTrait();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontWeightTrait", optional=true)
        public static native NSString WeightTrait();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontWidthTrait", optional=true)
        public static native NSString WidthTrait();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="UIFontSlantTrait", optional=true)
        public static native NSString SlantTrait();
    }
    /*</keys>*/
}

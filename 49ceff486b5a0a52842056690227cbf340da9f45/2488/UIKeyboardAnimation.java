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
@Marshaler(/*<name>*/UIKeyboardAnimation/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIKeyboardAnimation/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static UIKeyboardAnimation toObject(Class<UIKeyboardAnimation> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new UIKeyboardAnimation(o);
        }
        @MarshalsPointer
        public static long toNative(UIKeyboardAnimation o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<UIKeyboardAnimation> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<UIKeyboardAnimation> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new UIKeyboardAnimation(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<UIKeyboardAnimation> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (UIKeyboardAnimation i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    UIKeyboardAnimation(NSDictionary data) {
        super(data);
    }
    public UIKeyboardAnimation() {}
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
    public UIKeyboardAnimation set(NSString key, NSObject value) {
        data.put(key, value);
        return this;
    }
    

    /**
     * @since Available in iOS 3.2 and later.
     */
    public CGRect getStartFrame() {
        if (has(Keys.FrameBegin())) {
            NSValue val = (NSValue) get(Keys.FrameBegin());
            return val.rectValue();
        }
        return null;
    }
    /**
     * @since Available in iOS 3.2 and later.
     */
    public UIKeyboardAnimation setStartFrame(CGRect startFrame) {
        set(Keys.FrameBegin(), NSValue.valueOf(startFrame));
        return this;
    }
    /**
     * @since Available in iOS 3.2 and later.
     */
    public CGRect getEndFrame() {
        if (has(Keys.FrameEnd())) {
            NSValue val = (NSValue) get(Keys.FrameEnd());
            return val.rectValue();
        }
        return null;
    }
    /**
     * @since Available in iOS 3.2 and later.
     */
    public UIKeyboardAnimation setEndFrame(CGRect endFrame) {
        set(Keys.FrameEnd(), NSValue.valueOf(endFrame));
        return this;
    }
    /**
     * @since Available in iOS 3.0 and later.
     */
    public double getAnimationDuration() {
        if (has(Keys.AnimationDuration())) {
            NSNumber val = (NSNumber) get(Keys.AnimationDuration());
            return val.doubleValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 3.0 and later.
     */
    public UIKeyboardAnimation setAnimationDuration(double animationDuration) {
        set(Keys.AnimationDuration(), NSNumber.valueOf(animationDuration));
        return this;
    }
    /**
     * @since Available in iOS 3.0 and later.
     */
    public UIViewAnimationCurve getAnimationCurve() {
        if (has(Keys.AnimationCurve())) {
            NSNumber val = (NSNumber) get(Keys.AnimationCurve());
            return UIViewAnimationCurve.valueOf(val.longValue());
        }
        return null;
    }
    /**
     * @since Available in iOS 3.0 and later.
     */
    public UIKeyboardAnimation setAnimationCurve(UIViewAnimationCurve animationCurve) {
        set(Keys.AnimationCurve(), NSNumber.valueOf(animationCurve.value()));
        return this;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    public boolean isLocal() {
        if (has(Keys.IsLocal())) {
            NSNumber val = (NSNumber) get(Keys.IsLocal());
            return val.booleanValue();
        }
        return false;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    public UIKeyboardAnimation setLocal(boolean local) {
        set(Keys.IsLocal(), NSNumber.valueOf(local));
        return this;
    }
    /*</methods>*/
    
    /*<keys>*/
    @Library("UIKit")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        /**
         * @since Available in iOS 3.2 and later.
         */
        @GlobalValue(symbol="UIKeyboardFrameBeginUserInfoKey", optional=true)
        public static native NSString FrameBegin();
        /**
         * @since Available in iOS 3.2 and later.
         */
        @GlobalValue(symbol="UIKeyboardFrameEndUserInfoKey", optional=true)
        public static native NSString FrameEnd();
        /**
         * @since Available in iOS 3.0 and later.
         */
        @GlobalValue(symbol="UIKeyboardAnimationDurationUserInfoKey", optional=true)
        public static native NSString AnimationDuration();
        /**
         * @since Available in iOS 3.0 and later.
         */
        @GlobalValue(symbol="UIKeyboardAnimationCurveUserInfoKey", optional=true)
        public static native NSString AnimationCurve();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="UIKeyboardIsLocalUserInfoKey", optional=true)
        public static native NSString IsLocal();
    }
    /*</keys>*/
}

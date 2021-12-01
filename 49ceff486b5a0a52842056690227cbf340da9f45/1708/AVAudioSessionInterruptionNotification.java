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
package com.bugvm.apple.avfoundation;

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
import com.bugvm.apple.corefoundation.*;
import com.bugvm.apple.dispatch.*;
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coreimage.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coreaudio.*;
import com.bugvm.apple.coremedia.*;
import com.bugvm.apple.corevideo.*;
import com.bugvm.apple.mediatoolbox.*;
import com.bugvm.apple.audiotoolbox.*;
import com.bugvm.apple.audiounit.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("AVFoundation")/*</annotations>*/
@Marshaler(/*<name>*/AVAudioSessionInterruptionNotification/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVAudioSessionInterruptionNotification/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static AVAudioSessionInterruptionNotification toObject(Class<AVAudioSessionInterruptionNotification> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new AVAudioSessionInterruptionNotification(o);
        }
        @MarshalsPointer
        public static long toNative(AVAudioSessionInterruptionNotification o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<AVAudioSessionInterruptionNotification> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<AVAudioSessionInterruptionNotification> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new AVAudioSessionInterruptionNotification(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<AVAudioSessionInterruptionNotification> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (AVAudioSessionInterruptionNotification i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    AVAudioSessionInterruptionNotification(NSDictionary data) {
        super(data);
    }
    public AVAudioSessionInterruptionNotification() {}
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
    public AVAudioSessionInterruptionNotification set(NSString key, NSObject value) {
        data.put(key, value);
        return this;
    }
    

    /**
     * @since Available in iOS 6.0 and later.
     */
    public AVAudioSessionInterruptionType getType() {
        if (has(Keys.Type())) {
            NSNumber val = (NSNumber) get(Keys.Type());
            return AVAudioSessionInterruptionType.valueOf(val.longValue());
        }
        return null;
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    public AVAudioSessionInterruptionNotification setType(AVAudioSessionInterruptionType type) {
        set(Keys.Type(), NSNumber.valueOf(type.value()));
        return this;
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    public AVAudioSessionInterruptionOptions getOptions() {
        if (has(Keys.Option())) {
            NSNumber val = (NSNumber) get(Keys.Option());
            return new AVAudioSessionInterruptionOptions(val.longValue());
        }
        return null;
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    public AVAudioSessionInterruptionNotification setOptions(AVAudioSessionInterruptionOptions options) {
        set(Keys.Option(), NSNumber.valueOf(options.value()));
        return this;
    }
    /*</methods>*/
    
    /*<keys>*/
    @Library("AVFoundation")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        /**
         * @since Available in iOS 6.0 and later.
         */
        @GlobalValue(symbol="AVAudioSessionInterruptionTypeKey", optional=true)
        public static native NSString Type();
        /**
         * @since Available in iOS 6.0 and later.
         */
        @GlobalValue(symbol="AVAudioSessionInterruptionOptionKey", optional=true)
        public static native NSString Option();
    }
    /*</keys>*/
}

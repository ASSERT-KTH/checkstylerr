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
/*<annotations>*/@Library("AVFoundation") @StronglyLinked/*</annotations>*/
@Marshaler(/*<name>*/AVAudioTimePitchAlgorithm/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVAudioTimePitchAlgorithm/*</name>*/ 
    extends /*<extends>*/GlobalValueEnumeration<NSString>/*</extends>*/
    /*<implements>*//*</implements>*/ {

    static { Bro.bind(/*<name>*/AVAudioTimePitchAlgorithm/*</name>*/.class); }

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static AVAudioTimePitchAlgorithm toObject(Class<AVAudioTimePitchAlgorithm> cls, long handle, long flags) {
            NSString o = (NSString) NSObject.Marshaler.toObject(NSString.class, handle, flags);
            if (o == null) {
                return null;
            }
            return AVAudioTimePitchAlgorithm.valueOf(o);
        }
        @MarshalsPointer
        public static long toNative(AVAudioTimePitchAlgorithm o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.value(), flags);
        }
    }
    public static class AsListMarshaler {
        @SuppressWarnings("unchecked")
        @MarshalsPointer
        public static List<AVAudioTimePitchAlgorithm> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSString> o = (NSArray<NSString>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<AVAudioTimePitchAlgorithm> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(AVAudioTimePitchAlgorithm.valueOf(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<AVAudioTimePitchAlgorithm> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSString> array = new NSMutableArray<>();
            for (AVAudioTimePitchAlgorithm o : l) {
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
    public static final AVAudioTimePitchAlgorithm LowQualityZeroLatency = new AVAudioTimePitchAlgorithm("LowQualityZeroLatency");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVAudioTimePitchAlgorithm TimeDomain = new AVAudioTimePitchAlgorithm("TimeDomain");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVAudioTimePitchAlgorithm Spectral = new AVAudioTimePitchAlgorithm("Spectral");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVAudioTimePitchAlgorithm Varispeed = new AVAudioTimePitchAlgorithm("Varispeed");
    /*</constants>*/
    
    private static /*<name>*/AVAudioTimePitchAlgorithm/*</name>*/[] values = new /*<name>*/AVAudioTimePitchAlgorithm/*</name>*/[] {/*<value_list>*/LowQualityZeroLatency, TimeDomain, Spectral, Varispeed/*</value_list>*/};
    
    /*<name>*/AVAudioTimePitchAlgorithm/*</name>*/ (String getterName) {
        super(Values.class, getterName);
    }
    
    public static /*<name>*/AVAudioTimePitchAlgorithm/*</name>*/ valueOf(/*<type>*/NSString/*</type>*/ value) {
        for (/*<name>*/AVAudioTimePitchAlgorithm/*</name>*/ v : values) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + value + " found in " 
            + /*<name>*/AVAudioTimePitchAlgorithm/*</name>*/.class.getName());
    }
    
    /*<methods>*//*</methods>*/
    
    /*<annotations>*/@Library("AVFoundation") @StronglyLinked/*</annotations>*/
    public static class Values {
    	static { Bro.bind(Values.class); }

        /*<values>*/
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVAudioTimePitchAlgorithmLowQualityZeroLatency", optional=true)
        public static native NSString LowQualityZeroLatency();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVAudioTimePitchAlgorithmTimeDomain", optional=true)
        public static native NSString TimeDomain();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVAudioTimePitchAlgorithmSpectral", optional=true)
        public static native NSString Spectral();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVAudioTimePitchAlgorithmVarispeed", optional=true)
        public static native NSString Varispeed();
        /*</values>*/
    }
}

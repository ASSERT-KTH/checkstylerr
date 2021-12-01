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
@Marshaler(/*<name>*/AVMetadataExtraAttributes/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVMetadataExtraAttributes/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static AVMetadataExtraAttributes toObject(Class<AVMetadataExtraAttributes> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new AVMetadataExtraAttributes(o);
        }
        @MarshalsPointer
        public static long toNative(AVMetadataExtraAttributes o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<AVMetadataExtraAttributes> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<AVMetadataExtraAttributes> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new AVMetadataExtraAttributes(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<AVMetadataExtraAttributes> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (AVMetadataExtraAttributes i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    AVMetadataExtraAttributes(NSDictionary data) {
        super(data);
    }
    public AVMetadataExtraAttributes() {}
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
    public AVMetadataExtraAttributes set(NSString key, NSObject value) {
        data.put(key, value);
        return this;
    }
    

    /**
     * @since Available in iOS 8.0 and later.
     */
    public String getValueURI() {
        if (has(Keys.ValueURI())) {
            NSString val = (NSString) get(Keys.ValueURI());
            return val.toString();
        }
        return null;
    }
    /**
     * @since Available in iOS 8.0 and later.
     */
    public AVMetadataExtraAttributes setValueURI(String valueURI) {
        set(Keys.ValueURI(), new NSString(valueURI));
        return this;
    }
    /**
     * @since Available in iOS 8.0 and later.
     */
    public String getBaseURI() {
        if (has(Keys.BaseURI())) {
            NSString val = (NSString) get(Keys.BaseURI());
            return val.toString();
        }
        return null;
    }
    /**
     * @since Available in iOS 8.0 and later.
     */
    public AVMetadataExtraAttributes setBaseURI(String baseURI) {
        set(Keys.BaseURI(), new NSString(baseURI));
        return this;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    public String getInfo() {
        if (has(Keys.Info())) {
            NSString val = (NSString) get(Keys.Info());
            return val.toString();
        }
        return null;
    }
    /**
     * @since Available in iOS 9.0 and later.
     */
    public AVMetadataExtraAttributes setInfo(String info) {
        set(Keys.Info(), new NSString(info));
        return this;
    }
    /*</methods>*/
    
    /*<keys>*/
    @Library("AVFoundation")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="AVMetadataExtraAttributeValueURIKey", optional=true)
        public static native NSString ValueURI();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="AVMetadataExtraAttributeBaseURIKey", optional=true)
        public static native NSString BaseURI();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="AVMetadataExtraAttributeInfoKey", optional=true)
        public static native NSString Info();
    }
    /*</keys>*/
}

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
@Marshaler(/*<name>*/AVMetadataObjectType/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVMetadataObjectType/*</name>*/ 
    extends /*<extends>*/GlobalValueEnumeration<NSString>/*</extends>*/
    /*<implements>*//*</implements>*/ {

    static { Bro.bind(/*<name>*/AVMetadataObjectType/*</name>*/.class); }

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static AVMetadataObjectType toObject(Class<AVMetadataObjectType> cls, long handle, long flags) {
            NSString o = (NSString) NSObject.Marshaler.toObject(NSString.class, handle, flags);
            if (o == null) {
                return null;
            }
            return AVMetadataObjectType.valueOf(o);
        }
        @MarshalsPointer
        public static long toNative(AVMetadataObjectType o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.value(), flags);
        }
    }
    public static class AsListMarshaler {
        @SuppressWarnings("unchecked")
        @MarshalsPointer
        public static List<AVMetadataObjectType> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSString> o = (NSArray<NSString>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<AVMetadataObjectType> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(AVMetadataObjectType.valueOf(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<AVMetadataObjectType> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSString> array = new NSMutableArray<>();
            for (AVMetadataObjectType o : l) {
                array.add(o.value());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constants>*/
    /**
     * @since Available in iOS 6.0 and later.
     */
    public static final AVMetadataObjectType Face = new AVMetadataObjectType("Face");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType UPCECode = new AVMetadataObjectType("UPCECode");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType Code39Code = new AVMetadataObjectType("Code39Code");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType Code39Mod43Code = new AVMetadataObjectType("Code39Mod43Code");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType EAN13Code = new AVMetadataObjectType("EAN13Code");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType EAN8Code = new AVMetadataObjectType("EAN8Code");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType Code93Code = new AVMetadataObjectType("Code93Code");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType Code128Code = new AVMetadataObjectType("Code128Code");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType PDF417Code = new AVMetadataObjectType("PDF417Code");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType QRCode = new AVMetadataObjectType("QRCode");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final AVMetadataObjectType AztecCode = new AVMetadataObjectType("AztecCode");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final AVMetadataObjectType Interleaved2of5Code = new AVMetadataObjectType("Interleaved2of5Code");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final AVMetadataObjectType ITF14Code = new AVMetadataObjectType("ITF14Code");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final AVMetadataObjectType DataMatrixCode = new AVMetadataObjectType("DataMatrixCode");
    /*</constants>*/
    
    private static /*<name>*/AVMetadataObjectType/*</name>*/[] values = new /*<name>*/AVMetadataObjectType/*</name>*/[] {/*<value_list>*/Face, UPCECode, Code39Code, Code39Mod43Code, EAN13Code, EAN8Code, Code93Code, Code128Code, PDF417Code, QRCode, AztecCode, Interleaved2of5Code, ITF14Code, DataMatrixCode/*</value_list>*/};
    
    /*<name>*/AVMetadataObjectType/*</name>*/ (String getterName) {
        super(Values.class, getterName);
    }
    
    public static /*<name>*/AVMetadataObjectType/*</name>*/ valueOf(/*<type>*/NSString/*</type>*/ value) {
        for (/*<name>*/AVMetadataObjectType/*</name>*/ v : values) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + value + " found in " 
            + /*<name>*/AVMetadataObjectType/*</name>*/.class.getName());
    }
    
    /*<methods>*//*</methods>*/
    
    /*<annotations>*/@Library("AVFoundation") @StronglyLinked/*</annotations>*/
    public static class Values {
    	static { Bro.bind(Values.class); }

        /*<values>*/
        /**
         * @since Available in iOS 6.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeFace", optional=true)
        public static native NSString Face();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeUPCECode", optional=true)
        public static native NSString UPCECode();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeCode39Code", optional=true)
        public static native NSString Code39Code();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeCode39Mod43Code", optional=true)
        public static native NSString Code39Mod43Code();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeEAN13Code", optional=true)
        public static native NSString EAN13Code();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeEAN8Code", optional=true)
        public static native NSString EAN8Code();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeCode93Code", optional=true)
        public static native NSString Code93Code();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeCode128Code", optional=true)
        public static native NSString Code128Code();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypePDF417Code", optional=true)
        public static native NSString PDF417Code();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeQRCode", optional=true)
        public static native NSString QRCode();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeAztecCode", optional=true)
        public static native NSString AztecCode();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeInterleaved2of5Code", optional=true)
        public static native NSString Interleaved2of5Code();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeITF14Code", optional=true)
        public static native NSString ITF14Code();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="AVMetadataObjectTypeDataMatrixCode", optional=true)
        public static native NSString DataMatrixCode();
        /*</values>*/
    }
}

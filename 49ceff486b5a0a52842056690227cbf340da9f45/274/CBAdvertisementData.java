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
package com.bugvm.apple.corebluetooth;

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
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("CoreBluetooth")/*</annotations>*/
@Marshaler(/*<name>*/CBAdvertisementData/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CBAdvertisementData/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static CBAdvertisementData toObject(Class<CBAdvertisementData> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new CBAdvertisementData(o);
        }
        @MarshalsPointer
        public static long toNative(CBAdvertisementData o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<CBAdvertisementData> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<CBAdvertisementData> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new CBAdvertisementData(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<CBAdvertisementData> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (CBAdvertisementData i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    CBAdvertisementData(NSDictionary data) {
        super(data);
    }
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
    

    public String getLocalName() {
        if (has(Keys.LocalName())) {
            NSString val = (NSString) get(Keys.LocalName());
            return val.toString();
        }
        return null;
    }
    public double getTxPowerLevel() {
        if (has(Keys.TxPowerLevel())) {
            NSNumber val = (NSNumber) get(Keys.TxPowerLevel());
            return val.doubleValue();
        }
        return 0;
    }
    public NSArray getServiceUUIDs() {
        if (has(Keys.ServiceUUIDs())) {
            NSArray val = (NSArray) get(Keys.ServiceUUIDs());
            return val;
        }
        return null;
    }
    public NSData getManufacturerData() {
        if (has(Keys.ManufacturerData())) {
            NSData val = (NSData) get(Keys.ManufacturerData());
            return val;
        }
        return null;
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    public NSArray<CBUUID> getOverflowServiceUUIDs() {
        if (has(Keys.OverflowServiceUUIDs())) {
            NSArray<CBUUID> val = (NSArray<CBUUID>) get(Keys.OverflowServiceUUIDs());
            return val;
        }
        return null;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public boolean isConnectable() {
        if (has(Keys.IsConnectable())) {
            NSNumber val = (NSNumber) get(Keys.IsConnectable());
            return val.booleanValue();
        }
        return false;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSArray<CBUUID> getSolicitedServiceUUIDs() {
        if (has(Keys.SolicitedServiceUUIDs())) {
            NSArray<CBUUID> val = (NSArray<CBUUID>) get(Keys.SolicitedServiceUUIDs());
            return val;
        }
        return null;
    }
    /*</methods>*/
    
    /*<keys>*/
    @Library("CoreBluetooth")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        @GlobalValue(symbol="CBAdvertisementDataLocalNameKey", optional=true)
        public static native NSString LocalName();
        @GlobalValue(symbol="CBAdvertisementDataTxPowerLevelKey", optional=true)
        public static native NSString TxPowerLevel();
        @GlobalValue(symbol="CBAdvertisementDataServiceUUIDsKey", optional=true)
        public static native NSString ServiceUUIDs();
        @GlobalValue(symbol="CBAdvertisementDataServiceDataKey", optional=true)
        public static native NSString ServiceData();
        @GlobalValue(symbol="CBAdvertisementDataManufacturerDataKey", optional=true)
        public static native NSString ManufacturerData();
        /**
         * @since Available in iOS 6.0 and later.
         */
        @GlobalValue(symbol="CBAdvertisementDataOverflowServiceUUIDsKey", optional=true)
        public static native NSString OverflowServiceUUIDs();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="CBAdvertisementDataIsConnectable", optional=true)
        public static native NSString IsConnectable();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="CBAdvertisementDataSolicitedServiceUUIDsKey", optional=true)
        public static native NSString SolicitedServiceUUIDs();
    }
    /*</keys>*/
}

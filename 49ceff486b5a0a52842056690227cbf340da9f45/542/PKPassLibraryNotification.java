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
package com.bugvm.apple.passkit;

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
import com.bugvm.apple.uikit.*;
import com.bugvm.apple.addressbook.*;
import com.bugvm.apple.contacts.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("PassKit")/*</annotations>*/
@Marshaler(/*<name>*/PKPassLibraryNotification/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/PKPassLibraryNotification/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static PKPassLibraryNotification toObject(Class<PKPassLibraryNotification> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new PKPassLibraryNotification(o);
        }
        @MarshalsPointer
        public static long toNative(PKPassLibraryNotification o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<PKPassLibraryNotification> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<PKPassLibraryNotification> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new PKPassLibraryNotification(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<PKPassLibraryNotification> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (PKPassLibraryNotification i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    PKPassLibraryNotification(NSDictionary data) {
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
    

    /**
     * @since Available in iOS 6.0 and later.
     */
    public NSArray<PKPass> getAddedPasses() {
        if (has(Keys.AddedPasses())) {
            NSArray<PKPass> val = (NSArray<PKPass>) get(Keys.AddedPasses());
            return val;
        }
        return null;
    }
    /**
     * @since Available in iOS 6.0 and later.
     */
    public NSArray<PKPass> getReplacementPasses() {
        if (has(Keys.ReplacementPasses())) {
            NSArray<PKPass> val = (NSArray<PKPass>) get(Keys.ReplacementPasses());
            return val;
        }
        return null;
    }
    /*</methods>*/
    /**
     * @since Available in iOS 6.0 and later.
     */
    @SuppressWarnings("unchecked")
    public List<PKRemovedPassInfo> getRemovedPassInfos() {
        if (has(Keys.RemovedPassInfos())) {
            NSArray<NSDictionary<NSString, NSObject>> val = (NSArray<NSDictionary<NSString, NSObject>>) get(Keys.RemovedPassInfos());
            List<PKRemovedPassInfo> list = new ArrayList<>();
            for (NSDictionary<NSString, NSObject> v : val) {
                list.add(new PKRemovedPassInfo(v));
            }
            return list;
        }
        return null;
    }
    
    /*<keys>*/
    @Library("PassKit")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        /**
         * @since Available in iOS 6.0 and later.
         */
        @GlobalValue(symbol="PKPassLibraryAddedPassesUserInfoKey", optional=true)
        public static native NSString AddedPasses();
        /**
         * @since Available in iOS 6.0 and later.
         */
        @GlobalValue(symbol="PKPassLibraryReplacementPassesUserInfoKey", optional=true)
        public static native NSString ReplacementPasses();
        /**
         * @since Available in iOS 6.0 and later.
         */
        @GlobalValue(symbol="PKPassLibraryRemovedPassInfosUserInfoKey", optional=true)
        public static native NSString RemovedPassInfos();
    }
    /*</keys>*/
}

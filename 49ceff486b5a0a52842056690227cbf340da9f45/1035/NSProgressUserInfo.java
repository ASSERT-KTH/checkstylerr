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
package com.bugvm.apple.foundation;

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
import com.bugvm.apple.corefoundation.*;
import com.bugvm.apple.uikit.*;
import com.bugvm.apple.coretext.*;
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coredata.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coremedia.*;
import com.bugvm.apple.security.*;
import com.bugvm.apple.dispatch.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("Foundation")/*</annotations>*/
@Marshaler(/*<name>*/NSProgressUserInfo/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSProgressUserInfo/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static NSProgressUserInfo toObject(Class<NSProgressUserInfo> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new NSProgressUserInfo(o);
        }
        @MarshalsPointer
        public static long toNative(NSProgressUserInfo o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<NSProgressUserInfo> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<NSProgressUserInfo> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new NSProgressUserInfo(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<NSProgressUserInfo> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (NSProgressUserInfo i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    NSProgressUserInfo(NSDictionary data) {
        super(data);
    }
    public NSProgressUserInfo() {}
    /*</constructors>*/

    
    public boolean has(String key) {
        return data.containsKey(new NSString(key));
    }
    public NSObject get(String key) {
        if (has(key)) {
            return data.get(new NSString(key));
        }
        return null;
    }
    public NSProgressUserInfo set(String key, NSObject value) {
        data.put(new NSString(key), value);
        return this;
    }
    /*<methods>*/
    public boolean has(NSProgressUserInfoKey key) {
        return data.containsKey(key.value());
    }
    public NSObject get(NSProgressUserInfoKey key) {
        if (has(key)) {
            return data.get(key.value());
        }
        return null;
    }
    public NSProgressUserInfo set(NSProgressUserInfoKey key, NSObject value) {
        data.put(key.value(), value);
        return this;
    }
    

    /**
     * @since Available in iOS 7.0 and later.
     */
    public double getEstimatedTimeRemaining() {
        if (has(NSProgressUserInfoKey.EstimatedTimeRemaining)) {
            NSNumber val = (NSNumber) get(NSProgressUserInfoKey.EstimatedTimeRemaining);
            return val.doubleValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSProgressUserInfo setEstimatedTimeRemaining(double estimatedTimeRemaining) {
        set(NSProgressUserInfoKey.EstimatedTimeRemaining, NSNumber.valueOf(estimatedTimeRemaining));
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public long getThroughput() {
        if (has(NSProgressUserInfoKey.Throughput)) {
            NSNumber val = (NSNumber) get(NSProgressUserInfoKey.Throughput);
            return val.longValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSProgressUserInfo setThroughput(long throughput) {
        set(NSProgressUserInfoKey.Throughput, NSNumber.valueOf(throughput));
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSProgressFileOperationKind getFileOperationKind() {
        if (has(NSProgressUserInfoKey.FileOperationKind)) {
            NSString val = (NSString) get(NSProgressUserInfoKey.FileOperationKind);
            return NSProgressFileOperationKind.valueOf(val);
        }
        return null;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSProgressUserInfo setFileOperationKind(NSProgressFileOperationKind fileOperationKind) {
        set(NSProgressUserInfoKey.FileOperationKind, fileOperationKind.value());
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSURL getFileURL() {
        if (has(NSProgressUserInfoKey.FileURL)) {
            NSURL val = (NSURL) get(NSProgressUserInfoKey.FileURL);
            return val;
        }
        return null;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSProgressUserInfo setFileURL(NSURL fileURL) {
        set(NSProgressUserInfoKey.FileURL, fileURL);
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public long getTotalFileCount() {
        if (has(NSProgressUserInfoKey.FileTotalCount)) {
            NSNumber val = (NSNumber) get(NSProgressUserInfoKey.FileTotalCount);
            return val.longValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSProgressUserInfo setTotalFileCount(long totalFileCount) {
        set(NSProgressUserInfoKey.FileTotalCount, NSNumber.valueOf(totalFileCount));
        return this;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public long getCompletedFileCount() {
        if (has(NSProgressUserInfoKey.FileCompletedCount)) {
            NSNumber val = (NSNumber) get(NSProgressUserInfoKey.FileCompletedCount);
            return val.longValue();
        }
        return 0;
    }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public NSProgressUserInfo setCompletedFileCount(long completedFileCount) {
        set(NSProgressUserInfoKey.FileCompletedCount, NSNumber.valueOf(completedFileCount));
        return this;
    }
    /*</methods>*/
    
    /*<keys>*/
    /*</keys>*/
}

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
/*<annotations>*/@Library("Foundation") @StronglyLinked/*</annotations>*/
@Marshaler(/*<name>*/NSURLUbiquitousItemProperty/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSURLUbiquitousItemProperty/*</name>*/ 
    extends /*<extends>*/NSURLProperty/*</extends>*/
    /*<implements>*//*</implements>*/ {

    static { Bro.bind(/*<name>*/NSURLUbiquitousItemProperty/*</name>*/.class); }

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static NSURLUbiquitousItemProperty toObject(Class<NSURLUbiquitousItemProperty> cls, long handle, long flags) {
            NSString o = (NSString) NSObject.Marshaler.toObject(NSString.class, handle, flags);
            if (o == null) {
                return null;
            }
            return NSURLUbiquitousItemProperty.valueOf(o);
        }
        @MarshalsPointer
        public static long toNative(NSURLUbiquitousItemProperty o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.value(), flags);
        }
    }
    public static class AsListMarshaler {
        @SuppressWarnings("unchecked")
        @MarshalsPointer
        public static List<NSURLUbiquitousItemProperty> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSString> o = (NSArray<NSString>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<NSURLUbiquitousItemProperty> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(NSURLUbiquitousItemProperty.valueOf(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<NSURLUbiquitousItemProperty> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSString> array = new NSMutableArray<>();
            for (NSURLUbiquitousItemProperty o : l) {
                array.add(o.value());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constants>*/
    /**
     * @since Available in iOS 5.0 and later.
     */
    public static final NSURLUbiquitousItemProperty IsUbiquitousItem = new NSURLUbiquitousItemProperty("IsUbiquitousItem");
    /**
     * @since Available in iOS 5.0 and later.
     */
    public static final NSURLUbiquitousItemProperty HasUnresolvedConflicts = new NSURLUbiquitousItemProperty("HasUnresolvedConflicts");
    /**
     * @since Available in iOS 5.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    public static final NSURLUbiquitousItemProperty IsDownloaded = new NSURLUbiquitousItemProperty("IsDownloaded");
    /**
     * @since Available in iOS 5.0 and later.
     */
    public static final NSURLUbiquitousItemProperty IsDownloading = new NSURLUbiquitousItemProperty("IsDownloading");
    /**
     * @since Available in iOS 5.0 and later.
     */
    public static final NSURLUbiquitousItemProperty IsUploaded = new NSURLUbiquitousItemProperty("IsUploaded");
    /**
     * @since Available in iOS 5.0 and later.
     */
    public static final NSURLUbiquitousItemProperty IsUploading = new NSURLUbiquitousItemProperty("IsUploading");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSURLUbiquitousItemProperty DownloadingStatus = new NSURLUbiquitousItemProperty("DownloadingStatus");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSURLUbiquitousItemProperty DownloadingError = new NSURLUbiquitousItemProperty("DownloadingError");
    /**
     * @since Available in iOS 7.0 and later.
     */
    public static final NSURLUbiquitousItemProperty UploadingError = new NSURLUbiquitousItemProperty("UploadingError");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final NSURLUbiquitousItemProperty DownloadRequested = new NSURLUbiquitousItemProperty("DownloadRequested");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final NSURLUbiquitousItemProperty ContainerDisplayName = new NSURLUbiquitousItemProperty("ContainerDisplayName");
    /*</constants>*/
    
    private static /*<name>*/NSURLUbiquitousItemProperty/*</name>*/[] values = new /*<name>*/NSURLUbiquitousItemProperty/*</name>*/[] {/*<value_list>*/IsUbiquitousItem, HasUnresolvedConflicts, IsDownloaded, IsDownloading, IsUploaded, IsUploading, DownloadingStatus, DownloadingError, UploadingError, DownloadRequested, ContainerDisplayName/*</value_list>*/};
    
    /*<name>*/NSURLUbiquitousItemProperty/*</name>*/ (String getterName) {
        super(Values.class, getterName);
    }
    
    public static /*<name>*/NSURLUbiquitousItemProperty/*</name>*/ valueOf(/*<type>*/NSString/*</type>*/ value) {
        for (/*<name>*/NSURLUbiquitousItemProperty/*</name>*/ v : values) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + value + " found in " 
            + /*<name>*/NSURLUbiquitousItemProperty/*</name>*/.class.getName());
    }
    
    /*<methods>*//*</methods>*/
    
    /*<annotations>*/@Library("Foundation") @StronglyLinked/*</annotations>*/
    public static class Values {
    	static { Bro.bind(Values.class); }

        /*<values>*/
        /**
         * @since Available in iOS 5.0 and later.
         */
        @GlobalValue(symbol="NSURLIsUbiquitousItemKey", optional=true)
        public static native NSString IsUbiquitousItem();
        /**
         * @since Available in iOS 5.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemHasUnresolvedConflictsKey", optional=true)
        public static native NSString HasUnresolvedConflicts();
        /**
         * @since Available in iOS 5.0 and later.
         * @deprecated Deprecated in iOS 7.0.
         */
        @Deprecated
        @GlobalValue(symbol="NSURLUbiquitousItemIsDownloadedKey", optional=true)
        public static native NSString IsDownloaded();
        /**
         * @since Available in iOS 5.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemIsDownloadingKey", optional=true)
        public static native NSString IsDownloading();
        /**
         * @since Available in iOS 5.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemIsUploadedKey", optional=true)
        public static native NSString IsUploaded();
        /**
         * @since Available in iOS 5.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemIsUploadingKey", optional=true)
        public static native NSString IsUploading();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemDownloadingStatusKey", optional=true)
        public static native NSString DownloadingStatus();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemDownloadingErrorKey", optional=true)
        public static native NSString DownloadingError();
        /**
         * @since Available in iOS 7.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemUploadingErrorKey", optional=true)
        public static native NSString UploadingError();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemDownloadRequestedKey", optional=true)
        public static native NSString DownloadRequested();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="NSURLUbiquitousItemContainerDisplayNameKey", optional=true)
        public static native NSString ContainerDisplayName();
        /*</values>*/
    }
}

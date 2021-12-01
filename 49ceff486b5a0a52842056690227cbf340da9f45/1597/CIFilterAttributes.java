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
package com.bugvm.apple.coreimage;

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
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.opengles.*;
import com.bugvm.apple.corevideo.*;
import com.bugvm.apple.imageio.*;
import com.bugvm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("CoreImage")/*</annotations>*/
@Marshaler(/*<name>*/CIFilterAttributes/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CIFilterAttributes/*</name>*/ 
    extends /*<extends>*/NSDictionaryWrapper/*</extends>*/
    /*<implements>*//*</implements>*/ {

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static CIFilterAttributes toObject(Class<CIFilterAttributes> cls, long handle, long flags) {
            NSDictionary o = (NSDictionary) NSObject.Marshaler.toObject(NSDictionary.class, handle, flags);
            if (o == null) {
                return null;
            }
            return new CIFilterAttributes(o);
        }
        @MarshalsPointer
        public static long toNative(CIFilterAttributes o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.data, flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<CIFilterAttributes> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSDictionary> o = (NSArray<NSDictionary>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<CIFilterAttributes> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(new CIFilterAttributes(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<CIFilterAttributes> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSDictionary> array = new NSMutableArray<>();
            for (CIFilterAttributes i : l) {
                array.add(i.getDictionary());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constructors>*/
    CIFilterAttributes(NSDictionary data) {
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
    

    public String getName() {
        if (has(Keys.Name())) {
            NSString val = (NSString) get(Keys.Name());
            return val.toString();
        }
        return null;
    }
    public String getDisplayName() {
        if (has(Keys.DisplayName())) {
            NSString val = (NSString) get(Keys.DisplayName());
            return val.toString();
        }
        return null;
    }
    /*</methods>*/
    
    @SuppressWarnings("unchecked")
    public CIFilterAttribute getAttribute(String name) {
        NSString str = new NSString(name);
        if (has(str)) {
            NSDictionary<NSString, NSObject> val = (NSDictionary<NSString, NSObject>)get(str);
            return new CIFilterAttribute(val);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public List<CIFilterCategory> getCategories() {
        if (has(Keys.Categories())) {
            NSArray<NSString> val = (NSArray<NSString>)get(Keys.Categories());
            List<CIFilterCategory> list = new ArrayList<>();
            for (NSString str : val) {
                list.add(CIFilterCategory.valueOf(str));
            }
            return list;
        }
        return null;
    }
    
    /*<keys>*/
    @Library("CoreImage")
    public static class Keys {
        static { Bro.bind(Keys.class); }
        @GlobalValue(symbol="kCIAttributeFilterName", optional=true)
        public static native NSString Name();
        @GlobalValue(symbol="kCIAttributeFilterDisplayName", optional=true)
        public static native NSString DisplayName();
        @GlobalValue(symbol="kCIAttributeFilterCategories", optional=true)
        public static native NSString Categories();
    }
    /*</keys>*/
}

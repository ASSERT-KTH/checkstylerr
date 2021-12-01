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
package com.bugvm.apple.coreservices;

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
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*/@Library("CFNetwork") @StronglyLinked/*</annotations>*/
@Marshaler(/*<name>*/CFHTTPVersion/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CFHTTPVersion/*</name>*/ 
    extends /*<extends>*/GlobalValueEnumeration<CFString>/*</extends>*/
    /*<implements>*//*</implements>*/ {

    static { Bro.bind(/*<name>*/CFHTTPVersion/*</name>*/.class); }

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static CFHTTPVersion toObject(Class<CFHTTPVersion> cls, long handle, long flags) {
            CFString o = (CFString) CFType.Marshaler.toObject(CFString.class, handle, flags);
            if (o == null) {
                return null;
            }
            return CFHTTPVersion.valueOf(o);
        }
        @MarshalsPointer
        public static long toNative(CFHTTPVersion o, long flags) {
            if (o == null) {
                return 0L;
            }
            return CFType.Marshaler.toNative(o.value(), flags);
        }
    }
    public static class AsListMarshaler {
        @MarshalsPointer
        public static List<CFHTTPVersion> toObject(Class<? extends CFType> cls, long handle, long flags) {
            CFArray o = (CFArray) CFType.Marshaler.toObject(CFArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<CFHTTPVersion> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(CFHTTPVersion.valueOf(o.get(i, CFString.class)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<CFHTTPVersion> l, long flags) {
            if (l == null) {
                return 0L;
            }
            CFArray array = CFMutableArray.create();
            for (CFHTTPVersion o : l) {
                array.add(o.value());
            }
            return CFType.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constants>*/
    /**
     * @since Available in iOS 2.0 and later.
     */
    public static final CFHTTPVersion Version1_0 = new CFHTTPVersion("Version1_0");
    /**
     * @since Available in iOS 2.0 and later.
     */
    public static final CFHTTPVersion Version1_1 = new CFHTTPVersion("Version1_1");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final CFHTTPVersion Version2_0 = new CFHTTPVersion("Version2_0");
    /*</constants>*/
    
    private static /*<name>*/CFHTTPVersion/*</name>*/[] values = new /*<name>*/CFHTTPVersion/*</name>*/[] {/*<value_list>*/Version1_0, Version1_1, Version2_0/*</value_list>*/};
    
    /*<name>*/CFHTTPVersion/*</name>*/ (String getterName) {
        super(Values.class, getterName);
    }
    
    public static /*<name>*/CFHTTPVersion/*</name>*/ valueOf(/*<type>*/CFString/*</type>*/ value) {
        for (/*<name>*/CFHTTPVersion/*</name>*/ v : values) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + value + " found in " 
            + /*<name>*/CFHTTPVersion/*</name>*/.class.getName());
    }
    
    /*<methods>*//*</methods>*/
    
    /*<annotations>*/@Library("CFNetwork") @StronglyLinked/*</annotations>*/
    public static class Values {
    	static { Bro.bind(Values.class); }

        /*<values>*/
        /**
         * @since Available in iOS 2.0 and later.
         */
        @GlobalValue(symbol="kCFHTTPVersion1_0", optional=true)
        public static native CFString Version1_0();
        /**
         * @since Available in iOS 2.0 and later.
         */
        @GlobalValue(symbol="kCFHTTPVersion1_1", optional=true)
        public static native CFString Version1_1();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="kCFHTTPVersion2_0", optional=true)
        public static native CFString Version2_0();
        /*</values>*/
    }
}

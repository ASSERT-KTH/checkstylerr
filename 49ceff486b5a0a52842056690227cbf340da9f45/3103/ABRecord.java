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
package com.bugvm.apple.addressbook;

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
@Marshaler(ABRecord.Marshaler.class)
/*<annotations>*/@Library("AddressBook")/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/ABRecord/*</name>*/ 
    extends /*<extends>*/CFType/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/static { Bro.bind(ABRecord.class); }/*</bind>*/

    public static class Marshaler {
        @MarshalsPointer
        public static ABRecord toObject(Class<? extends ABRecord> cls, long handle, long flags) {
            return toObject(cls, handle, flags, true);
        }
        static ABRecord toObject(Class<? extends ABRecord> cls, long handle, long flags, boolean retain) {
            if (handle == 0) {
                return null;
            }
            int recordType = getRecordType(handle);
            Class<? extends ABRecord> subcls = null;
            switch (recordType) {
            case 0: // kABPersonType
                subcls = ABPerson.class;
                break;
            case 1: // kABGroupType
                subcls = ABGroup.class;
                break;
            case 2: // kABSourceType
                subcls = ABSource.class;
                break;
            default:
                throw new Error("Unrecognized record type " + recordType);
            }
            ABRecord o = (ABRecord) NativeObject.Marshaler.toObject(subcls, handle, flags);
            if (retain) {
                retain(handle);
            }
            return o;
        }
    }
    public static class NoRetainMarshaler {
        @MarshalsPointer
        public static ABRecord toObject(Class<? extends ABRecord> cls, long handle, long flags) {
            return Marshaler.toObject(cls, handle, flags, false);
        }
    }

    /*<constants>*/
    public static final int InvalidID = -1;
    /*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*//*</members>*/

    @Bridge(symbol="ABRecordGetRecordType", optional=true)
    static native int getRecordType(@Pointer long handle);

    public <T extends NativeObject> T getValue(ABProperty property, Class<T> type) {
        CFType val = getValue(property);
        if (val != null) return val.as(type);
        return null;
    }
    /*<methods>*/
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Bridge(symbol="ABRecordGetRecordID", optional=true)
    public native int getRecordID();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Bridge(symbol="ABRecordGetRecordType", optional=true)
    public native ABRecordType getRecordType();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Bridge(symbol="ABRecordCopyValue", optional=true)
    public native @com.bugvm.rt.bro.annotation.Marshaler(CFType.NoRetainMarshaler.class) CFType getValue(ABProperty property);
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    public boolean setValue(ABProperty property, CFType value) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       boolean result = setValue(property, value, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Bridge(symbol="ABRecordSetValue", optional=true)
    private native boolean setValue(ABProperty property, CFType value, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    public boolean removeValue(ABProperty property) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       boolean result = removeValue(property, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Bridge(symbol="ABRecordRemoveValue", optional=true)
    private native boolean removeValue(ABProperty property, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Bridge(symbol="ABRecordCopyCompositeName", optional=true)
    public native @com.bugvm.rt.bro.annotation.Marshaler(CFString.AsStringNoRetainMarshaler.class) String getCompositeName();
    /*</methods>*/
}

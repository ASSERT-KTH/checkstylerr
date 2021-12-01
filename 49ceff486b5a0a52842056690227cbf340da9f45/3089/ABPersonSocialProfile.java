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
/*<annotations>*/@Library("AddressBook")/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/ABPersonSocialProfile/*</name>*/ 
    extends /*<extends>*/CocoaUtility/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    private CFDictionary data;
    private CFString label;
    
    public ABPersonSocialProfile(String label) {
        this.data = CFMutableDictionary.create();
        this.label = new CFString(label);
    }
    public ABPersonSocialProfile(ABPropertyLabel label) {
        this.data = CFMutableDictionary.create();
        this.label = label.value();
    }
    protected ABPersonSocialProfile(CFDictionary data, CFString label) {
        this.data = data;
        this.label = label;
    }
    /*<bind>*/static { Bro.bind(ABPersonSocialProfile.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*//*</properties>*/
    /*<members>*//*</members>*/
    public CFDictionary getDictionary() {
        return data;
    }
    
    public String getLabel() {
        return label.toString();
    }
    protected CFString getLabel0() {
        return label;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public String getURL() {
        if (data.containsKey(URLKey())) {
            CFString val = data.get(URLKey(), CFString.class);
            return val.toString();
        }
        return null;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public ABPersonSocialProfile setURL(String url) {
        data.put(URLKey(), new CFString(url));
        return this;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public ABPersonSocialProfileService getService() {
        if (data.containsKey(ServiceKey())) {
            CFString val = data.get(ServiceKey(), CFString.class);
            return ABPersonSocialProfileService.valueOf(val);
        }
        return null;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public ABPersonSocialProfile setService(ABPersonSocialProfileService service) {
        data.put(ServiceKey(), service.value());
        return this;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public String getUsername() {
        if (data.containsKey(UsernameKey())) {
            CFString val = data.get(UsernameKey(), CFString.class);
            return val.toString();
        }
        return null;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public ABPersonSocialProfile setUsername(String username) {
        data.put(UsernameKey(), new CFString(username));
        return this;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public String getUserIdentifier() {
        if (data.containsKey(UserIdentifierKey())) {
            CFString val = data.get(UserIdentifierKey(), CFString.class);
            return val.toString();
        }
        return null;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    public ABPersonSocialProfile setUserIdentifier(String userIdentifier) {
        data.put(UserIdentifierKey(), new CFString(userIdentifier));
        return this;
    }
    /*<methods>*/
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @GlobalValue(symbol="kABPersonSocialProfileURLKey", optional=true)
    protected static native CFString URLKey();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @GlobalValue(symbol="kABPersonSocialProfileServiceKey", optional=true)
    protected static native CFString ServiceKey();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @GlobalValue(symbol="kABPersonSocialProfileUsernameKey", optional=true)
    protected static native CFString UsernameKey();
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @GlobalValue(symbol="kABPersonSocialProfileUserIdentifierKey", optional=true)
    protected static native CFString UserIdentifierKey();
    /*</methods>*/
    @Override
    public String toString() {
        if(data != null) return data.toString();
        return super.toString();
    }
}

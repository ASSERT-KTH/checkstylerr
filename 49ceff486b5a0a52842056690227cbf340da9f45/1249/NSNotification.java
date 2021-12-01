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
/*<annotations>*/@Library("Foundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSNotification/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements NSCoding/*</implements>*/ {

    /*<ptr>*/public static class NSNotificationPtr extends Ptr<NSNotification, NSNotificationPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSNotification.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected NSNotification(SkipInit skipInit) { super(skipInit); }
    /**
     * @since Available in iOS 4.0 and later.
     */
    public NSNotification(NSString name, NSObject object, NSDictionary<?, ?> userInfo) { super((SkipInit) null); initObject(init(name, object, userInfo)); }
    public NSNotification(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    public NSNotification(String name, NSObject object, NSDictionary<?, ?> userInfo) {
        super((SkipInit) null);
        initObject(init(new NSString(name), object, userInfo));
    }
    public NSNotification(NSString name, NSObject object, UIRemoteNotification userInfo) {
        super((SkipInit) null);
        initObject(init(name, object, userInfo.getDictionary()));
    }
    public NSNotification(String name, NSObject object, UIRemoteNotification userInfo) {
        super((SkipInit) null);
        initObject(init(new NSString(name), object, userInfo.getDictionary()));
    }
    public NSNotification(NSString name, NSObject object, UILocalNotification userInfo) {
        super((SkipInit) null);
        initObject(init(name, object, userInfo.getUserInfo()));
    }
    public NSNotification(String name, NSObject object, UILocalNotification userInfo) {
        super((SkipInit) null);
        initObject(init(new NSString(name), object, userInfo.getUserInfo()));
    }
    /*<properties>*/
    @Property(selector = "name")
    public native String getName();
    @Property(selector = "object")
    public native NSObject getObject();
    @Property(selector = "userInfo")
    public native NSDictionary<?, ?> getUserInfo();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 4.0 and later.
     */
    @Method(selector = "initWithName:object:userInfo:")
    protected native @Pointer long init(NSString name, NSObject object, NSDictionary<?, ?> userInfo);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    @Method(selector = "encodeWithCoder:")
    public native void encode(NSCoder coder);
    /*</methods>*/
}

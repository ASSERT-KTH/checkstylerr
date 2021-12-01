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
package com.bugvm.apple.uikit;

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
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coredata.*;
import com.bugvm.apple.coreimage.*;
import com.bugvm.apple.coretext.*;
import com.bugvm.apple.corelocation.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 2.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UITabBarItem/*</name>*/ 
    extends /*<extends>*/UIBarItem/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class UITabBarItemPtr extends Ptr<UITabBarItem, UITabBarItemPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UITabBarItem.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UITabBarItem() {}
    protected UITabBarItem(SkipInit skipInit) { super(skipInit); }
    public UITabBarItem(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    public UITabBarItem(String title, UIImage image, @MachineSizedSInt long tag) { super((SkipInit) null); initObject(init(title, image, tag)); }
    /**
     * @since Available in iOS 7.0 and later.
     */
    public UITabBarItem(String title, UIImage image, UIImage selectedImage) { super((SkipInit) null); initObject(init(title, image, selectedImage)); }
    public UITabBarItem(UITabBarSystemItem systemItem, @MachineSizedSInt long tag) { super((SkipInit) null); initObject(init(systemItem, tag)); }
    /*</constructors>*/
    /*<properties>*/
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "selectedImage")
    public native UIImage getSelectedImage();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setSelectedImage:")
    public native void setSelectedImage(UIImage v);
    @Property(selector = "badgeValue")
    public native String getBadgeValue();
    @Property(selector = "setBadgeValue:")
    public native void setBadgeValue(String v);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "titlePositionAdjustment")
    public native @ByVal UIOffset getTitlePositionAdjustment();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setTitlePositionAdjustment:")
    public native void setTitlePositionAdjustment(@ByVal UIOffset v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    @Method(selector = "initWithTitle:image:tag:")
    protected native @Pointer long init(String title, UIImage image, @MachineSizedSInt long tag);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "initWithTitle:image:selectedImage:")
    protected native @Pointer long init(String title, UIImage image, UIImage selectedImage);
    @Method(selector = "initWithTabBarSystemItem:tag:")
    protected native @Pointer long init(UITabBarSystemItem systemItem, @MachineSizedSInt long tag);
    /**
     * @since Available in iOS 5.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Method(selector = "setFinishedSelectedImage:withFinishedUnselectedImage:")
    public native void setFinishedImages(UIImage selectedImage, UIImage unselectedImage);
    /**
     * @since Available in iOS 5.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Method(selector = "finishedSelectedImage")
    public native UIImage getFinishedSelectedImage();
    /**
     * @since Available in iOS 5.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Method(selector = "finishedUnselectedImage")
    public native UIImage getFinishedUnselectedImage();
    /*</methods>*/
}

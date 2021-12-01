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
 * @since Available in iOS 8.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIPopoverPresentationController/*</name>*/ 
    extends /*<extends>*/UIPresentationController/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class UIPopoverPresentationControllerPtr extends Ptr<UIPopoverPresentationController, UIPopoverPresentationControllerPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UIPopoverPresentationController.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UIPopoverPresentationController() {}
    protected UIPopoverPresentationController(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "delegate")
    public native UIPopoverPresentationControllerDelegate getPopoverPresentationDelegate();
    @Property(selector = "setDelegate:", strongRef = true)
    public native void setPopoverPresentationDelegate(UIPopoverPresentationControllerDelegate v);
    @Property(selector = "permittedArrowDirections")
    public native UIPopoverArrowDirection getPermittedArrowDirections();
    @Property(selector = "setPermittedArrowDirections:")
    public native void setPermittedArrowDirections(UIPopoverArrowDirection v);
    @Property(selector = "sourceView")
    public native UIView getSourceView();
    @Property(selector = "setSourceView:")
    public native void setSourceView(UIView v);
    @Property(selector = "sourceRect")
    public native @ByVal CGRect getSourceRect();
    @Property(selector = "setSourceRect:")
    public native void setSourceRect(@ByVal CGRect v);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "canOverlapSourceViewRect")
    public native boolean canOverlapSourceViewRect();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "setCanOverlapSourceViewRect:")
    public native void setCanOverlapSourceViewRect(boolean v);
    @Property(selector = "barButtonItem")
    public native UIBarButtonItem getBarButtonItem();
    @Property(selector = "setBarButtonItem:")
    public native void setBarButtonItem(UIBarButtonItem v);
    @Property(selector = "arrowDirection")
    public native UIPopoverArrowDirection getArrowDirection();
    @Property(selector = "passthroughViews")
    public native NSArray<UIView> getPassthroughViews();
    @Property(selector = "setPassthroughViews:")
    public native void setPassthroughViews(NSArray<UIView> v);
    @Property(selector = "backgroundColor")
    public native UIColor getBackgroundColor();
    @Property(selector = "setBackgroundColor:")
    public native void setBackgroundColor(UIColor v);
    @Property(selector = "popoverLayoutMargins")
    public native @ByVal UIEdgeInsets getPopoverLayoutMargins();
    @Property(selector = "setPopoverLayoutMargins:")
    public native void setPopoverLayoutMargins(@ByVal UIEdgeInsets v);
    @Property(selector = "popoverBackgroundViewClass")
    public native Class<? extends UIPopoverBackgroundView> getPopoverBackgroundViewClass();
    @Property(selector = "setPopoverBackgroundViewClass:")
    public native void setPopoverBackgroundViewClass(Class<? extends UIPopoverBackgroundView> v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    
    /*</methods>*/
}

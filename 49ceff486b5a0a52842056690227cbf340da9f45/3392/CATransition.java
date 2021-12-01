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
package com.bugvm.apple.coreanimation;

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
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coreimage.*;
import com.bugvm.apple.coretext.*;
import com.bugvm.apple.opengles.*;
import com.bugvm.apple.metal.*;
/*</imports>*/

/*<javadoc>*/

/*</javadoc>*/
/*<annotations>*/@Library("QuartzCore") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/CATransition/*</name>*/ 
    extends /*<extends>*/CAAnimation/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class CATransitionPtr extends Ptr<CATransition, CATransitionPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(CATransition.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public CATransition() {}
    protected CATransition(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "type")
    public native CATransitionType getType();
    @Property(selector = "setType:")
    public native void setType(CATransitionType v);
    @Property(selector = "subtype")
    public native CATransitionSubType getSubtype();
    @Property(selector = "setSubtype:")
    public native void setSubtype(CATransitionSubType v);
    @Property(selector = "startProgress")
    public native float getStartProgress();
    @Property(selector = "setStartProgress:")
    public native void setStartProgress(float v);
    @Property(selector = "endProgress")
    public native float getEndProgress();
    @Property(selector = "setEndProgress:")
    public native void setEndProgress(float v);
    @WeaklyLinked
    @Property(selector = "filter")
    public native CIFilter getFilter();
    @WeaklyLinked
    @Property(selector = "setFilter:")
    public native void setFilter(CIFilter v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    
    /*</methods>*/
}

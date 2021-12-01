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
 * @since Available in iOS 6.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("UIKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSParagraphStyle/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class NSParagraphStylePtr extends Ptr<NSParagraphStyle, NSParagraphStylePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSParagraphStyle.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSParagraphStyle() {}
    protected NSParagraphStyle(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "lineSpacing")
    public native @MachineSizedFloat double getLineSpacing();
    @Property(selector = "paragraphSpacing")
    public native @MachineSizedFloat double getParagraphSpacing();
    @Property(selector = "alignment")
    public native NSTextAlignment getAlignment();
    @Property(selector = "headIndent")
    public native @MachineSizedFloat double getHeadIndent();
    @Property(selector = "tailIndent")
    public native @MachineSizedFloat double getTailIndent();
    @Property(selector = "firstLineHeadIndent")
    public native @MachineSizedFloat double getFirstLineHeadIndent();
    @Property(selector = "minimumLineHeight")
    public native @MachineSizedFloat double getMinimumLineHeight();
    @Property(selector = "maximumLineHeight")
    public native @MachineSizedFloat double getMaximumLineHeight();
    @Property(selector = "lineBreakMode")
    public native NSLineBreakMode getLineBreakMode();
    @Property(selector = "baseWritingDirection")
    public native NSWritingDirection getBaseWritingDirection();
    @Property(selector = "lineHeightMultiple")
    public native @MachineSizedFloat double getLineHeightMultiple();
    @Property(selector = "paragraphSpacingBefore")
    public native @MachineSizedFloat double getParagraphSpacingBefore();
    @Property(selector = "hyphenationFactor")
    public native float getHyphenationFactor();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "tabStops")
    public native NSArray<NSTextTab> getTabStops();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "defaultTabInterval")
    public native @MachineSizedFloat double getDefaultTabInterval();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "allowsDefaultTighteningForTruncation")
    public native boolean allowsDefaultTighteningForTruncation();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "defaultParagraphStyle")
    public static native NSParagraphStyle getDefaultParagraphStyle();
    @Method(selector = "defaultWritingDirectionForLanguage:")
    public static native NSWritingDirection getDefaultWritingDirection(String languageName);
    /*</methods>*/
}

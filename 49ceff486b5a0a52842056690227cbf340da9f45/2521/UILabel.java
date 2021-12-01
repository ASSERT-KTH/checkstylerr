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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UILabel/*</name>*/ 
    extends /*<extends>*/UIView/*</extends>*/ 
    /*<implements>*/implements NSCoding/*</implements>*/ {

    /*<ptr>*/public static class UILabelPtr extends Ptr<UILabel, UILabelPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UILabel.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UILabel() {}
    protected UILabel(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    
    public UILabel(CGRect frame) {
        super(frame);
    }
    
    /*<properties>*/
    @Property(selector = "text")
    public native String getText();
    @Property(selector = "setText:")
    public native void setText(String v);
    @Property(selector = "font")
    public native UIFont getFont();
    @Property(selector = "setFont:")
    public native void setFont(UIFont v);
    @Property(selector = "textColor")
    public native UIColor getTextColor();
    @Property(selector = "setTextColor:")
    public native void setTextColor(UIColor v);
    @Property(selector = "shadowColor")
    public native UIColor getShadowColor();
    @Property(selector = "setShadowColor:")
    public native void setShadowColor(UIColor v);
    @Property(selector = "shadowOffset")
    public native @ByVal CGSize getShadowOffset();
    @Property(selector = "setShadowOffset:")
    public native void setShadowOffset(@ByVal CGSize v);
    @Property(selector = "textAlignment")
    public native NSTextAlignment getTextAlignment();
    @Property(selector = "setTextAlignment:")
    public native void setTextAlignment(NSTextAlignment v);
    @Property(selector = "lineBreakMode")
    public native NSLineBreakMode getLineBreakMode();
    @Property(selector = "setLineBreakMode:")
    public native void setLineBreakMode(NSLineBreakMode v);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "attributedText")
    public native NSAttributedString getAttributedText();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "setAttributedText:")
    public native void setAttributedText(NSAttributedString v);
    @Property(selector = "highlightedTextColor")
    public native UIColor getHighlightedTextColor();
    @Property(selector = "setHighlightedTextColor:")
    public native void setHighlightedTextColor(UIColor v);
    @Property(selector = "isHighlighted")
    public native boolean isHighlighted();
    @Property(selector = "setHighlighted:")
    public native void setHighlighted(boolean v);
    @Property(selector = "isUserInteractionEnabled")
    public native boolean isUserInteractionEnabled();
    @Property(selector = "setUserInteractionEnabled:")
    public native void setUserInteractionEnabled(boolean v);
    @Property(selector = "isEnabled")
    public native boolean isEnabled();
    @Property(selector = "setEnabled:")
    public native void setEnabled(boolean v);
    @Property(selector = "numberOfLines")
    public native @MachineSizedSInt long getNumberOfLines();
    @Property(selector = "setNumberOfLines:")
    public native void setNumberOfLines(@MachineSizedSInt long v);
    @Property(selector = "adjustsFontSizeToFitWidth")
    public native boolean adjustsFontSizeToFitWidth();
    @Property(selector = "setAdjustsFontSizeToFitWidth:")
    public native void setAdjustsFontSizeToFitWidth(boolean v);
    @Property(selector = "baselineAdjustment")
    public native UIBaselineAdjustment getBaselineAdjustment();
    @Property(selector = "setBaselineAdjustment:")
    public native void setBaselineAdjustment(UIBaselineAdjustment v);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "minimumScaleFactor")
    public native @MachineSizedFloat double getMinimumScaleFactor();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "setMinimumScaleFactor:")
    public native void setMinimumScaleFactor(@MachineSizedFloat double v);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "allowsDefaultTighteningForTruncation")
    public native boolean allowsDefaultTighteningForTruncation();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "setAllowsDefaultTighteningForTruncation:")
    public native void setAllowsDefaultTighteningForTruncation(boolean v);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "preferredMaxLayoutWidth")
    public native @MachineSizedFloat double getPreferredMaxLayoutWidth();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "setPreferredMaxLayoutWidth:")
    public native void setPreferredMaxLayoutWidth(@MachineSizedFloat double v);
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "adjustsLetterSpacingToFitWidth")
    public native boolean adjustsLetterSpacingToFitWidth();
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "setAdjustsLetterSpacingToFitWidth:")
    public native void setAdjustsLetterSpacingToFitWidth(boolean v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "textRectForBounds:limitedToNumberOfLines:")
    public native @ByVal CGRect getTextRect(@ByVal CGRect bounds, @MachineSizedSInt long numberOfLines);
    @Method(selector = "drawTextInRect:")
    public native void drawText(@ByVal CGRect rect);
    /*</methods>*/
}

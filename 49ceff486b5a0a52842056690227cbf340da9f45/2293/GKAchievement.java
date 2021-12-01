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
package com.bugvm.apple.gamekit;

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
import com.bugvm.apple.uikit.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 4.1 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("GameKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/GKAchievement/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements NSCoding/*</implements>*/ {

    /*<ptr>*/public static class GKAchievementPtr extends Ptr<GKAchievement, GKAchievementPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(GKAchievement.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public GKAchievement() {}
    protected GKAchievement(SkipInit skipInit) { super(skipInit); }
    public GKAchievement(String identifier) { super((SkipInit) null); initObject(init(identifier)); }
    /**
     * @since Available in iOS 8.0 and later.
     */
    public GKAchievement(String identifier, GKPlayer player) { super((SkipInit) null); initObject(init(identifier, player)); }
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public GKAchievement(String identifier, String playerID) { super((SkipInit) null); initObject(init(identifier, playerID)); }
    public GKAchievement(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "identifier")
    public native String getIdentifier();
    @Property(selector = "setIdentifier:")
    public native void setIdentifier(String v);
    @Property(selector = "percentComplete")
    public native double getPercentComplete();
    @Property(selector = "setPercentComplete:")
    public native void setPercentComplete(double v);
    @Property(selector = "isCompleted")
    public native boolean isCompleted();
    @Property(selector = "lastReportedDate")
    public native NSDate getLastReportedDate();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "showsCompletionBanner")
    public native boolean showsCompletionBanner();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setShowsCompletionBanner:")
    public native void setShowsCompletionBanner(boolean v);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "player")
    public native GKPlayer getPlayer();
    /**
     * @since Available in iOS 4.1 and later.
     * @deprecated Deprecated in iOS 6.0.
     */
    @Deprecated
    @Property(selector = "isHidden")
    public native boolean isHidden();
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Property(selector = "playerID")
    public native String getPlayerID();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithIdentifier:")
    protected native @Pointer long init(String identifier);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "initWithIdentifier:player:")
    protected native @Pointer long init(String identifier, GKPlayer player);
    @Method(selector = "loadAchievementsWithCompletionHandler:")
    public static native void loadAchievements(@Block VoidBlock2<NSArray<GKAchievement>, NSError> completionHandler);
    @Method(selector = "resetAchievementsWithCompletionHandler:")
    public static native void resetAchievements(@Block VoidBlock1<NSError> completionHandler);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "reportAchievements:withCompletionHandler:")
    public static native void reportAchievements(NSArray<GKAchievement> achievements, @Block VoidBlock1<NSError> completionHandler);
    /**
     * @since Available in iOS 4.1 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Method(selector = "reportAchievementWithCompletionHandler:")
    public native void reportAchievement(@Block VoidBlock1<NSError> completionHandler);
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Method(selector = "initWithIdentifier:forPlayer:")
    protected native @Pointer long init(String identifier, String playerID);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "challengeComposeControllerWithMessage:players:completionHandler:")
    public native UIViewController getChallengeComposeController(String message, NSArray<GKPlayer> players, @Block VoidBlock3<UIViewController, Boolean, NSArray<GKPlayer>> completionHandler);
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Method(selector = "issueChallengeToPlayers:message:")
    public native void issueChallengeToPlayers(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> playerIDs, String message);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "selectChallengeablePlayers:withCompletionHandler:")
    public native void selectChallengeablePlayers(NSArray<GKPlayer> players, @Block VoidBlock2<NSArray<GKPlayer>, NSError> completionHandler);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "reportAchievements:withEligibleChallenges:withCompletionHandler:")
    public static native void reportAchievements(NSArray<GKAchievement> achievements, NSArray<GKChallenge> challenges, @Block VoidBlock1<NSError> completionHandler);
    /**
     * @since Available in iOS 6.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Method(selector = "selectChallengeablePlayerIDs:withCompletionHandler:")
    public native void selectChallengeablePlayerIDs(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> playerIDs, @Block VoidBlock2<NSArray<NSString>, NSError> completionHandler);
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Method(selector = "challengeComposeControllerWithPlayers:message:completionHandler:")
    public native UIViewController getChallengeComposeController(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> playerIDs, String message, @Block VoidBlock3<UIViewController, Boolean, NSArray<NSString>> completionHandler);
    @Method(selector = "encodeWithCoder:")
    public native void encode(NSCoder coder);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    /*</methods>*/
}

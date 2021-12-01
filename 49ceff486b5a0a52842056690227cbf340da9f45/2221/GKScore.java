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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/GKScore/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements NSCoding/*</implements>*/ {

    /*<ptr>*/public static class GKScorePtr extends Ptr<GKScore, GKScorePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(GKScore.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public GKScore() {}
    protected GKScore(SkipInit skipInit) { super(skipInit); }
    public GKScore(String identifier) { super((SkipInit) null); initObject(init(identifier)); }
    /**
     * @since Available in iOS 8.0 and later.
     */
    public GKScore(String identifier, GKPlayer player) { super((SkipInit) null); initObject(init(identifier, player)); }
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    public GKScore(String identifier, String playerID) { super((SkipInit) null); initObject(init(identifier, playerID)); }
    public GKScore(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "value")
    public native long getValue();
    @Property(selector = "setValue:")
    public native void setValue(long v);
    @Property(selector = "formattedValue")
    public native String getFormattedValue();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "leaderboardIdentifier")
    public native String getLeaderboardIdentifier();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setLeaderboardIdentifier:")
    public native void setLeaderboardIdentifier(String v);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "context")
    public native long getContext();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setContext:")
    public native void setContext(long v);
    @Property(selector = "date")
    public native NSDate getDate();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "player")
    public native GKPlayer getPlayer();
    @Property(selector = "rank")
    public native @MachineSizedSInt long getRank();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "shouldSetDefaultLeaderboard")
    public native boolean shouldSetDefaultLeaderboard();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setShouldSetDefaultLeaderboard:")
    public native void setShouldSetDefaultLeaderboard(boolean v);
    /**
     * @since Available in iOS 4.1 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Property(selector = "playerID")
    public native String getPlayerID();
    /**
     * @since Available in iOS 4.1 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "category")
    public native String getCategory();
    /**
     * @since Available in iOS 4.1 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Property(selector = "setCategory:")
    public native void setCategory(String v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithLeaderboardIdentifier:")
    protected native @Pointer long init(String identifier);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "initWithLeaderboardIdentifier:player:")
    protected native @Pointer long init(String identifier, GKPlayer player);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "reportScores:withCompletionHandler:")
    public static native void reportScores(NSArray<GKScore> scores, @Block VoidBlock1<NSError> completionHandler);
    /**
     * @since Available in iOS 7.0 and later.
     * @deprecated Deprecated in iOS 8.0.
     */
    @Deprecated
    @Method(selector = "initWithLeaderboardIdentifier:forPlayer:")
    protected native @Pointer long init(String identifier, String playerID);
    /**
     * @since Available in iOS 4.1 and later.
     * @deprecated Deprecated in iOS 7.0.
     */
    @Deprecated
    @Method(selector = "reportScoreWithCompletionHandler:")
    public native void reportScore(@Block VoidBlock1<NSError> completionHandler);
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
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "reportScores:withEligibleChallenges:withCompletionHandler:")
    public static native void reportScores(NSArray<GKScore> scores, NSArray<GKChallenge> challenges, @Block VoidBlock1<NSError> completionHandler);
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

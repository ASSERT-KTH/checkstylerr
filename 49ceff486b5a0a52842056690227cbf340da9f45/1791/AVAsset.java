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
package com.bugvm.apple.avfoundation;

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
import com.bugvm.apple.dispatch.*;
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coreimage.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coreaudio.*;
import com.bugvm.apple.coremedia.*;
import com.bugvm.apple.corevideo.*;
import com.bugvm.apple.mediatoolbox.*;
import com.bugvm.apple.audiotoolbox.*;
import com.bugvm.apple.audiounit.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 4.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("AVFoundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVAsset/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements AVAsynchronousKeyValueLoading/*</implements>*/ {

    public static class Notifications {
        /**
         * @since Available in iOS 9.0 and later.
         */
        public static NSObject observeDurationDidChange(AVAsset object, final VoidBlock1<AVAsset> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(DurationDidChangeNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification notification) {
                    block.invoke((AVAsset) notification.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 9.0 and later.
         */
        public static NSObject observeChapterMetadataGroupsDidChange(AVAsset object, final VoidBlock1<AVAsset> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(ChapterMetadataGroupsDidChangeNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification notification) {
                    block.invoke((AVAsset) notification.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 9.0 and later.
         */
        public static NSObject observeMediaSelectionGroupsDidChange(AVAsset object, final VoidBlock1<AVAsset> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(MediaSelectionGroupsDidChangeNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification notification) {
                    block.invoke((AVAsset) notification.getObject());
                }
            });
        }
    }
    
    /*<ptr>*/public static class AVAssetPtr extends Ptr<AVAsset, AVAssetPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(AVAsset.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public AVAsset() {}
    protected AVAsset(long handle) { super(handle); }
    protected AVAsset(SkipInit skipInit) { super(skipInit); }
    public AVAsset(NSURL URL) { super(create(URL)); retain(getHandle()); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "duration")
    public native @ByVal CMTime getDuration();
    @Property(selector = "preferredRate")
    public native float getPreferredRate();
    @Property(selector = "preferredVolume")
    public native float getPreferredVolume();
    @Property(selector = "preferredTransform")
    public native @ByVal CGAffineTransform getPreferredTransform();
    @Property(selector = "providesPreciseDurationAndTiming")
    public native boolean providesPreciseDurationAndTiming();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "referenceRestrictions")
    public native AVAssetReferenceRestrictions getReferenceRestrictions();
    @Property(selector = "tracks")
    public native NSArray<? extends AVAssetTrack> getTracks();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "trackGroups")
    public native NSArray<AVAssetTrackGroup> getTrackGroups();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "creationDate")
    public native AVMetadataItem getCreationDate();
    @Property(selector = "lyrics")
    public native String getLyrics();
    @Property(selector = "commonMetadata")
    public native NSArray<AVMetadataItem> getCommonMetadata();
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Property(selector = "metadata")
    public native NSArray<AVMetadataItem> getMetadata();
    @Property(selector = "availableMetadataFormats")
    public native @com.bugvm.rt.bro.annotation.Marshaler(AVMetadataFormat.AsListMarshaler.class) List<AVMetadataFormat> getAvailableMetadataFormats();
    /**
     * @since Available in iOS 4.3 and later.
     */
    @Property(selector = "availableChapterLocales")
    public native NSArray<NSLocale> getAvailableChapterLocales();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "availableMediaCharacteristicsWithMediaSelectionOptions")
    public native @com.bugvm.rt.bro.annotation.Marshaler(AVMediaCharacteristic.AsListMarshaler.class) List<AVMediaCharacteristic> getAvailableMediaCharacteristicsWithMediaSelectionOptions();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "preferredMediaSelection")
    public native AVMediaSelection getPreferredMediaSelection();
    /**
     * @since Available in iOS 4.2 and later.
     */
    @Property(selector = "hasProtectedContent")
    public native boolean hasProtectedContent();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "canContainFragments")
    public native boolean canContainFragments();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "containsFragments")
    public native boolean containsFragments();
    /**
     * @since Available in iOS 4.3 and later.
     */
    @Property(selector = "isPlayable")
    public native boolean isPlayable();
    /**
     * @since Available in iOS 4.3 and later.
     */
    @Property(selector = "isExportable")
    public native boolean isExportable();
    /**
     * @since Available in iOS 4.3 and later.
     */
    @Property(selector = "isReadable")
    public native boolean isReadable();
    /**
     * @since Available in iOS 4.3 and later.
     */
    @Property(selector = "isComposable")
    public native boolean isComposable();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "isCompatibleWithSavedPhotosAlbum")
    public native boolean isCompatibleWithSavedPhotosAlbum();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "isCompatibleWithAirPlayVideo")
    public native boolean isCompatibleWithAirPlayVideo();
    /*</properties>*/
    /*<members>*//*</members>*/
    /**
     * 
     * @param key
     * @return
     * @throws NSErrorException
     */
    public AVKeyValueStatus getStatusOfValue(AVMetadataKey key) throws NSErrorException {
        NSError.NSErrorPtr err = new NSError.NSErrorPtr();
        AVKeyValueStatus result = getStatusOfValue(key, err);
        if (err.get() != null) {
            throw new NSErrorException(err.get());
        }
        return result;
    }
    /*<methods>*/
    /**
     * @since Available in iOS 9.0 and later.
     */
    @GlobalValue(symbol="AVAssetDurationDidChangeNotification", optional=true)
    public static native NSString DurationDidChangeNotification();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @GlobalValue(symbol="AVAssetChapterMetadataGroupsDidChangeNotification", optional=true)
    public static native NSString ChapterMetadataGroupsDidChangeNotification();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @GlobalValue(symbol="AVAssetMediaSelectionGroupsDidChangeNotification", optional=true)
    public static native NSString MediaSelectionGroupsDidChangeNotification();
    
    @Method(selector = "assetWithURL:")
    protected static native @Pointer long create(NSURL URL);
    @Method(selector = "cancelLoading")
    public native void cancelLoading();
    @Method(selector = "trackWithTrackID:")
    public native AVAssetTrack getTrack(int trackID);
    @Method(selector = "tracksWithMediaType:")
    public native NSArray<AVAssetTrack> getTracksWithType(AVMediaType mediaType);
    @Method(selector = "tracksWithMediaCharacteristic:")
    public native NSArray<AVAssetTrack> getTracksWithCharacteristic(AVMediaCharacteristic mediaCharacteristic);
    @Method(selector = "metadataForFormat:")
    public native NSArray<AVMetadataItem> getMetadata(AVMetadataFormat format);
    /**
     * @since Available in iOS 4.3 and later.
     */
    @Method(selector = "chapterMetadataGroupsWithTitleLocale:containingItemsWithCommonKeys:")
    public native NSArray<AVTimedMetadataGroup> getChapterMetadataGroupsContainingItemsWithCommonKeys(NSLocale locale, @com.bugvm.rt.bro.annotation.Marshaler(AVMetadataKey.AsListMarshaler.class) List<AVMetadataKey> commonKeys);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Method(selector = "chapterMetadataGroupsBestMatchingPreferredLanguages:")
    public native NSArray<AVTimedMetadataGroup> getChapterMetadataGroupsBestMatchingPreferredLanguages(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> preferredLanguages);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "mediaSelectionGroupForMediaCharacteristic:")
    public native AVMediaSelectionGroup getMediaSelectionGroup(AVMediaCharacteristic mediaCharacteristic);
    @Method(selector = "unusedTrackID")
    public native int getUnusedTrackID();
    @Method(selector = "statusOfValueForKey:error:")
    public native AVKeyValueStatus getStatusOfValue(AVMetadataKey key, NSError.NSErrorPtr outError);
    @Method(selector = "loadValuesAsynchronouslyForKeys:completionHandler:")
    public native void loadValuesAsynchronously(@com.bugvm.rt.bro.annotation.Marshaler(AVMetadataKey.AsListMarshaler.class) List<AVMetadataKey> keys, @Block Runnable handler);
    /*</methods>*/
}

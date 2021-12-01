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
/*</javadoc>*/
/*<annotations>*//*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UICollectionViewDelegateAdapter/*</name>*/ 
    extends /*<extends>*/UIScrollViewDelegateAdapter/*</extends>*/ 
    /*<implements>*/implements UICollectionViewDelegate/*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @NotImplemented("collectionView:shouldHighlightItemAtIndexPath:")
    public boolean shouldHighlightItem(UICollectionView collectionView, NSIndexPath indexPath) { return false; }
    @NotImplemented("collectionView:didHighlightItemAtIndexPath:")
    public void didHighlightItem(UICollectionView collectionView, NSIndexPath indexPath) {}
    @NotImplemented("collectionView:didUnhighlightItemAtIndexPath:")
    public void didUnhighlightItem(UICollectionView collectionView, NSIndexPath indexPath) {}
    @NotImplemented("collectionView:shouldSelectItemAtIndexPath:")
    public boolean shouldSelectItem(UICollectionView collectionView, NSIndexPath indexPath) { return false; }
    @NotImplemented("collectionView:shouldDeselectItemAtIndexPath:")
    public boolean shouldDeselectItem(UICollectionView collectionView, NSIndexPath indexPath) { return false; }
    @NotImplemented("collectionView:didSelectItemAtIndexPath:")
    public void didSelectItem(UICollectionView collectionView, NSIndexPath indexPath) {}
    @NotImplemented("collectionView:didDeselectItemAtIndexPath:")
    public void didDeselectItem(UICollectionView collectionView, NSIndexPath indexPath) {}
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("collectionView:willDisplayCell:forItemAtIndexPath:")
    public void willDisplayCell(UICollectionView collectionView, UICollectionViewCell cell, NSIndexPath indexPath) {}
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("collectionView:willDisplaySupplementaryView:forElementKind:atIndexPath:")
    public void willDisplaySupplementaryView(UICollectionView collectionView, UICollectionReusableView view, String elementKind, NSIndexPath indexPath) {}
    @NotImplemented("collectionView:didEndDisplayingCell:forItemAtIndexPath:")
    public void didEndDisplayingCell(UICollectionView collectionView, UICollectionViewCell cell, NSIndexPath indexPath) {}
    @NotImplemented("collectionView:didEndDisplayingSupplementaryView:forElementOfKind:atIndexPath:")
    public void didEndDisplayingSupplementaryView(UICollectionView collectionView, UICollectionReusableView view, String elementKind, NSIndexPath indexPath) {}
    @NotImplemented("collectionView:shouldShowMenuForItemAtIndexPath:")
    public boolean shouldShowMenuForItem(UICollectionView collectionView, NSIndexPath indexPath) { return false; }
    @NotImplemented("collectionView:canPerformAction:forItemAtIndexPath:withSender:")
    public boolean canPerformAction(UICollectionView collectionView, Selector action, NSIndexPath indexPath, NSObject sender) { return false; }
    @NotImplemented("collectionView:performAction:forItemAtIndexPath:withSender:")
    public void performAction(UICollectionView collectionView, Selector action, NSIndexPath indexPath, NSObject sender) {}
    @NotImplemented("collectionView:transitionLayoutForOldLayout:newLayout:")
    public UICollectionViewTransitionLayout getTransitionLayout(UICollectionView collectionView, UICollectionViewLayout fromLayout, UICollectionViewLayout toLayout) { return null; }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @NotImplemented("collectionView:targetIndexPathForMoveFromItemAtIndexPath:toProposedIndexPath:")
    public NSIndexPath getTargetIndexPathForMoveFromItem(UICollectionView collectionView, NSIndexPath originalIndexPath, NSIndexPath proposedIndexPath) { return null; }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @NotImplemented("collectionView:targetContentOffsetForProposedContentOffset:")
    public @ByVal CGPoint getTargetContentOffsetForProposedContentOffset(UICollectionView collectionView, @ByVal CGPoint proposedContentOffset) { return null; }
    /*</methods>*/
}

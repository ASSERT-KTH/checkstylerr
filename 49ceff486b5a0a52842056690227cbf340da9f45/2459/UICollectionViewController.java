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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UICollectionViewController/*</name>*/ 
    extends /*<extends>*/UIViewController/*</extends>*/ 
    /*<implements>*/implements UICollectionViewDelegate, UICollectionViewDataSource/*</implements>*/ {

    /*<ptr>*/public static class UICollectionViewControllerPtr extends Ptr<UICollectionViewController, UICollectionViewControllerPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(UICollectionViewController.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public UICollectionViewController() {}
    protected UICollectionViewController(SkipInit skipInit) { super(skipInit); }
    public UICollectionViewController(UICollectionViewLayout layout) { super((SkipInit) null); initObject(init(layout)); }
    public UICollectionViewController(String nibNameOrNil, NSBundle nibBundleOrNil) { super((SkipInit) null); initObject(init(nibNameOrNil, nibBundleOrNil)); }
    public UICollectionViewController(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "collectionView")
    public native UICollectionView getCollectionView();
    @Property(selector = "setCollectionView:")
    public native void setCollectionView(UICollectionView v);
    @Property(selector = "clearsSelectionOnViewWillAppear")
    public native boolean clearsSelectionOnViewWillAppear();
    @Property(selector = "setClearsSelectionOnViewWillAppear:")
    public native void setClearsSelectionOnViewWillAppear(boolean v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "useLayoutToLayoutNavigationTransitions")
    public native boolean usesLayoutToLayoutNavigationTransitions();
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "setUseLayoutToLayoutNavigationTransitions:")
    public native void setUsesLayoutToLayoutNavigationTransitions(boolean v);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Property(selector = "collectionViewLayout")
    public native UICollectionViewLayout getCollectionViewLayout();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "installsStandardGestureForInteractiveMovement")
    public native boolean installsStandardGestureForInteractiveMovement();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "setInstallsStandardGestureForInteractiveMovement:")
    public native void setInstallsStandardGestureForInteractiveMovement(boolean v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithCollectionViewLayout:")
    protected native @Pointer long init(UICollectionViewLayout layout);
    @Method(selector = "initWithNibName:bundle:")
    protected native @Pointer long init(String nibNameOrNil, NSBundle nibBundleOrNil);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    @Method(selector = "collectionView:shouldHighlightItemAtIndexPath:")
    public native boolean shouldHighlightItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "collectionView:didHighlightItemAtIndexPath:")
    public native void didHighlightItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "collectionView:didUnhighlightItemAtIndexPath:")
    public native void didUnhighlightItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "collectionView:shouldSelectItemAtIndexPath:")
    public native boolean shouldSelectItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "collectionView:shouldDeselectItemAtIndexPath:")
    public native boolean shouldDeselectItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "collectionView:didSelectItemAtIndexPath:")
    public native void didSelectItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "collectionView:didDeselectItemAtIndexPath:")
    public native void didDeselectItem(UICollectionView collectionView, NSIndexPath indexPath);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "collectionView:willDisplayCell:forItemAtIndexPath:")
    public native void willDisplayCell(UICollectionView collectionView, UICollectionViewCell cell, NSIndexPath indexPath);
    /**
     * @since Available in iOS 8.0 and later.
     */
    @Method(selector = "collectionView:willDisplaySupplementaryView:forElementKind:atIndexPath:")
    public native void willDisplaySupplementaryView(UICollectionView collectionView, UICollectionReusableView view, String elementKind, NSIndexPath indexPath);
    @Method(selector = "collectionView:didEndDisplayingCell:forItemAtIndexPath:")
    public native void didEndDisplayingCell(UICollectionView collectionView, UICollectionViewCell cell, NSIndexPath indexPath);
    @Method(selector = "collectionView:didEndDisplayingSupplementaryView:forElementOfKind:atIndexPath:")
    public native void didEndDisplayingSupplementaryView(UICollectionView collectionView, UICollectionReusableView view, String elementKind, NSIndexPath indexPath);
    @Method(selector = "collectionView:shouldShowMenuForItemAtIndexPath:")
    public native boolean shouldShowMenuForItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "collectionView:canPerformAction:forItemAtIndexPath:withSender:")
    public native boolean canPerformAction(UICollectionView collectionView, Selector action, NSIndexPath indexPath, NSObject sender);
    @Method(selector = "collectionView:performAction:forItemAtIndexPath:withSender:")
    public native void performAction(UICollectionView collectionView, Selector action, NSIndexPath indexPath, NSObject sender);
    @Method(selector = "collectionView:transitionLayoutForOldLayout:newLayout:")
    public native UICollectionViewTransitionLayout getTransitionLayout(UICollectionView collectionView, UICollectionViewLayout fromLayout, UICollectionViewLayout toLayout);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "collectionView:targetIndexPathForMoveFromItemAtIndexPath:toProposedIndexPath:")
    public native NSIndexPath getTargetIndexPathForMoveFromItem(UICollectionView collectionView, NSIndexPath originalIndexPath, NSIndexPath proposedIndexPath);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "collectionView:targetContentOffsetForProposedContentOffset:")
    public native @ByVal CGPoint getTargetContentOffsetForProposedContentOffset(UICollectionView collectionView, @ByVal CGPoint proposedContentOffset);
    @Method(selector = "scrollViewDidScroll:")
    public native void didScroll(UIScrollView scrollView);
    /**
     * @since Available in iOS 3.2 and later.
     */
    @Method(selector = "scrollViewDidZoom:")
    public native void didZoom(UIScrollView scrollView);
    @Method(selector = "scrollViewWillBeginDragging:")
    public native void willBeginDragging(UIScrollView scrollView);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "scrollViewWillEndDragging:withVelocity:targetContentOffset:")
    public native void willEndDragging(UIScrollView scrollView, @ByVal CGPoint velocity, CGPoint targetContentOffset);
    @Method(selector = "scrollViewDidEndDragging:willDecelerate:")
    public native void didEndDragging(UIScrollView scrollView, boolean decelerate);
    @Method(selector = "scrollViewWillBeginDecelerating:")
    public native void willBeginDecelerating(UIScrollView scrollView);
    @Method(selector = "scrollViewDidEndDecelerating:")
    public native void didEndDecelerating(UIScrollView scrollView);
    @Method(selector = "scrollViewDidEndScrollingAnimation:")
    public native void didEndScrollingAnimation(UIScrollView scrollView);
    @Method(selector = "viewForZoomingInScrollView:")
    public native UIView getViewForZooming(UIScrollView scrollView);
    /**
     * @since Available in iOS 3.2 and later.
     */
    @Method(selector = "scrollViewWillBeginZooming:withView:")
    public native void willBeginZooming(UIScrollView scrollView, UIView view);
    @Method(selector = "scrollViewDidEndZooming:withView:atScale:")
    public native void didEndZooming(UIScrollView scrollView, UIView view, @MachineSizedFloat double scale);
    @Method(selector = "scrollViewShouldScrollToTop:")
    public native boolean shouldScrollToTop(UIScrollView scrollView);
    @Method(selector = "scrollViewDidScrollToTop:")
    public native void didScrollToTop(UIScrollView scrollView);
    @Method(selector = "collectionView:numberOfItemsInSection:")
    public native @MachineSizedSInt long getNumberOfItemsInSection(UICollectionView collectionView, @MachineSizedSInt long section);
    @Method(selector = "collectionView:cellForItemAtIndexPath:")
    public native UICollectionViewCell getCellForItem(UICollectionView collectionView, NSIndexPath indexPath);
    @Method(selector = "numberOfSectionsInCollectionView:")
    public native @MachineSizedSInt long getNumberOfSections(UICollectionView collectionView);
    @Method(selector = "collectionView:viewForSupplementaryElementOfKind:atIndexPath:")
    public native UICollectionReusableView getViewForSupplementaryElement(UICollectionView collectionView, String kind, NSIndexPath indexPath);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "collectionView:canMoveItemAtIndexPath:")
    public native boolean canMoveItemAt(UICollectionView collectionView, NSIndexPath indexPath);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "collectionView:moveItemAtIndexPath:toIndexPath:")
    public native void moveItemAt(UICollectionView collectionView, NSIndexPath sourceIndexPath, NSIndexPath destinationIndexPath);
    /*</methods>*/
}

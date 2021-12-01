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
package com.bugvm.apple.foundation;

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
import com.bugvm.apple.corefoundation.*;
import com.bugvm.apple.uikit.*;
import com.bugvm.apple.coretext.*;
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coredata.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coremedia.*;
import com.bugvm.apple.security.*;
import com.bugvm.apple.dispatch.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 3.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("Foundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSUndoManager/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    public static class Notifications {
        /**
         * @since Available in iOS 3.0 and later.
         */
        public static NSObject observeCheckpoint(NSUndoManager object, final VoidBlock1<NSUndoManager> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(CheckpointNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSUndoManager) a.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 3.0 and later.
         */
        public static NSObject observeWillUndoChange(NSUndoManager object, final VoidBlock1<NSUndoManager> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(WillUndoChangeNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSUndoManager) a.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 3.0 and later.
         */
        public static NSObject observeWillRedoChange(NSUndoManager object, final VoidBlock1<NSUndoManager> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(WillRedoChangeNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSUndoManager) a.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 3.0 and later.
         */
        public static NSObject observeDidUndoChange(NSUndoManager object, final VoidBlock1<NSUndoManager> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(DidUndoChangeNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSUndoManager) a.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 3.0 and later.
         */
        public static NSObject observeDidRedoChange(NSUndoManager object, final VoidBlock1<NSUndoManager> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(DidRedoChangeNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSUndoManager) a.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 3.0 and later.
         */
        public static NSObject observeDidOpenUndoGroup(NSUndoManager object, final VoidBlock1<NSUndoManager> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(DidOpenUndoGroupNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSUndoManager) a.getObject());
                }
            });
        }
        /**
         * @since Available in iOS 3.0 and later.
         */
        public static NSObject observeWillCloseUndoGroup(NSUndoManager object, final VoidBlock2<NSUndoManager, Boolean> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(WillCloseUndoGroupNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    boolean b = false;
                    NSDictionary<?, ?> data = a.getUserInfo();
                    if (data.containsKey(GroupIsDiscardableKey())) {
                        NSNumber val = (NSNumber)data.get(GroupIsDiscardableKey());
                        b = val.booleanValue();
                    }
                    block.invoke((NSUndoManager) a.getObject(), b);
                }
            });
        }
        /**
         * @since Available in iOS 5.0 and later.
         */
        public static NSObject observeDidCloseUndoGroup(NSUndoManager object, final VoidBlock1<NSUndoManager> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(DidCloseUndoGroupNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSUndoManager) a.getObject());
                }
            });
        }
    }
    
    
    
    /*<ptr>*/public static class NSUndoManagerPtr extends Ptr<NSUndoManager, NSUndoManagerPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSUndoManager.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSUndoManager() {}
    protected NSUndoManager(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    public void setRunLoopModes(NSRunLoopMode...modes) {
        List<String> list = new ArrayList<>();
        for (NSRunLoopMode mode : modes) {
            list.add(mode.value().toString());
        }
        setRunLoopModes(list);
    }
    
    /*<properties>*/
    @Property(selector = "groupingLevel")
    public native @MachineSizedSInt long getGroupingLevel();
    @Property(selector = "isUndoRegistrationEnabled")
    public native boolean isUndoRegistrationEnabled();
    @Property(selector = "groupsByEvent")
    public native boolean groupsByEvent();
    @Property(selector = "setGroupsByEvent:")
    public native void setGroupsByEvent(boolean v);
    @Property(selector = "levelsOfUndo")
    public native @MachineSizedUInt long getLevelsOfUndo();
    @Property(selector = "setLevelsOfUndo:")
    public native void setLevelsOfUndo(@MachineSizedUInt long v);
    @Property(selector = "runLoopModes")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> getRunLoopModes();
    @Property(selector = "setRunLoopModes:")
    public native void setRunLoopModes(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> v);
    @Property(selector = "canUndo")
    public native boolean canUndo();
    @Property(selector = "canRedo")
    public native boolean canRedo();
    @Property(selector = "isUndoing")
    public native boolean isUndoing();
    @Property(selector = "isRedoing")
    public native boolean isRedoing();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "undoActionIsDiscardable")
    public native boolean isUndoActionDiscardable();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "redoActionIsDiscardable")
    public native boolean isRedoActionDiscardable();
    @Property(selector = "undoActionName")
    public native String getUndoActionName();
    @Property(selector = "redoActionName")
    public native String getRedoActionName();
    @Property(selector = "undoMenuItemTitle")
    public native String getUndoMenuItemTitle();
    @Property(selector = "redoMenuItemTitle")
    public native String getRedoMenuItemTitle();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @GlobalValue(symbol="NSUndoCloseGroupingRunLoopOrdering", optional=true)
    public static native @MachineSizedUInt long getCloseGroupingRunLoopOrdering();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerGroupIsDiscardableKey", optional=true)
    protected static native NSString GroupIsDiscardableKey();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerCheckpointNotification", optional=true)
    public static native NSString CheckpointNotification();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerWillUndoChangeNotification", optional=true)
    public static native NSString WillUndoChangeNotification();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerWillRedoChangeNotification", optional=true)
    public static native NSString WillRedoChangeNotification();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerDidUndoChangeNotification", optional=true)
    public static native NSString DidUndoChangeNotification();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerDidRedoChangeNotification", optional=true)
    public static native NSString DidRedoChangeNotification();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerDidOpenUndoGroupNotification", optional=true)
    public static native NSString DidOpenUndoGroupNotification();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerWillCloseUndoGroupNotification", optional=true)
    public static native NSString WillCloseUndoGroupNotification();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @GlobalValue(symbol="NSUndoManagerDidCloseUndoGroupNotification", optional=true)
    public static native NSString DidCloseUndoGroupNotification();
    
    @Method(selector = "beginUndoGrouping")
    public native void beginUndoGrouping();
    @Method(selector = "endUndoGrouping")
    public native void endUndoGrouping();
    @Method(selector = "disableUndoRegistration")
    public native void disableUndoRegistration();
    @Method(selector = "enableUndoRegistration")
    public native void enableUndoRegistration();
    @Method(selector = "undo")
    public native void undo();
    @Method(selector = "redo")
    public native void redo();
    @Method(selector = "undoNestedGroup")
    public native void undoNestedGroup();
    @Method(selector = "removeAllActions")
    public native void removeAllActions();
    @Method(selector = "removeAllActionsWithTarget:")
    public native void removeAllActions(NSObject target);
    @Method(selector = "registerUndoWithTarget:selector:object:")
    public native void registerUndo(NSObject target, Selector selector, NSObject anObject);
    @Method(selector = "prepareWithInvocationTarget:")
    public native NSObject prepareWithInvocation(NSObject target);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "registerUndoWithTarget:handler:")
    public native void registerUndo(NSObject target, @Block VoidBlock1<NSObject> undoHandler);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Method(selector = "setActionIsDiscardable:")
    public native void setActionIsDiscardable(boolean discardable);
    @Method(selector = "setActionName:")
    public native void setActionName(String actionName);
    @Method(selector = "undoMenuTitleForUndoActionName:")
    public native String getUndoMenuTitle(String actionName);
    @Method(selector = "redoMenuTitleForUndoActionName:")
    public native String getRedoMenuTitle(String actionName);
    /*</methods>*/
}

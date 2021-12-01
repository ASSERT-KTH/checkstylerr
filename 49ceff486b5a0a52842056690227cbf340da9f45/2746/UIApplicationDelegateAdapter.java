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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/UIApplicationDelegateAdapter/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements UIApplicationDelegate/*</implements>*/ {

    private UIWindow window;

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*/
    
    /*</properties>*/
    /*<members>*//*</members>*/
    
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Override
    public UIWindow getWindow() { 
        return window;
    }
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Override
    public void setWindow(UIWindow window) {
        this.window = window;
    }
    
    @Override
    public void didReceiveMemoryWarning(UIApplication application) {
        /*
         * We have to tell the GC to free some memory when we get a memory warning.
         * Multiple calls to System.gc() will instruct the GC to start his work
         * immediately.
         */
        for (int i = 0; i < 10; i++) {
            System.gc();
        }
    }
    
    /*<methods>*/
    @NotImplemented("applicationDidFinishLaunching:")
    public void didFinishLaunching(UIApplication application) {}
    /**
     * @since Available in iOS 6.0 and later.
     */
    @NotImplemented("application:willFinishLaunchingWithOptions:")
    public boolean willFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) { return false; }
    /**
     * @since Available in iOS 3.0 and later.
     */
    @NotImplemented("application:didFinishLaunchingWithOptions:")
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) { return false; }
    @NotImplemented("applicationDidBecomeActive:")
    public void didBecomeActive(UIApplication application) {}
    @NotImplemented("applicationWillResignActive:")
    public void willResignActive(UIApplication application) {}
    /**
     * @since Available in iOS 2.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @NotImplemented("application:handleOpenURL:")
    public boolean handleOpenURL(UIApplication application, NSURL url) { return false; }
    /**
     * @since Available in iOS 4.2 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @NotImplemented("application:openURL:sourceApplication:annotation:")
    public boolean openURL(UIApplication application, NSURL url, String sourceApplication, NSPropertyList annotation) { return false; }
    /**
     * @since Available in iOS 9.0 and later.
     */
    @NotImplemented("application:openURL:options:")
    public boolean openURL(UIApplication app, NSURL url, UIApplicationOpenURLOptions options) { return false; }
    @NotImplemented("applicationWillTerminate:")
    public void willTerminate(UIApplication application) {}
    @NotImplemented("applicationSignificantTimeChange:")
    public void significantTimeChange(UIApplication application) {}
    @NotImplemented("application:willChangeStatusBarOrientation:duration:")
    public void willChangeStatusBarOrientation(UIApplication application, UIInterfaceOrientation newStatusBarOrientation, double duration) {}
    @NotImplemented("application:didChangeStatusBarOrientation:")
    public void didChangStatusBarOrientation(UIApplication application, UIInterfaceOrientation oldStatusBarOrientation) {}
    @NotImplemented("application:willChangeStatusBarFrame:")
    public void willChangeStatusBarFrame(UIApplication application, @ByVal CGRect newStatusBarFrame) {}
    @NotImplemented("application:didChangeStatusBarFrame:")
    public void didChangStatusBarFrame(UIApplication application, @ByVal CGRect oldStatusBarFrame) {}
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:didRegisterUserNotificationSettings:")
    public void didRegisterUserNotificationSettings(UIApplication application, UIUserNotificationSettings notificationSettings) {}
    /**
     * @since Available in iOS 3.0 and later.
     */
    @NotImplemented("application:didRegisterForRemoteNotificationsWithDeviceToken:")
    public void didRegisterForRemoteNotifications(UIApplication application, NSData deviceToken) {}
    /**
     * @since Available in iOS 3.0 and later.
     */
    @NotImplemented("application:didFailToRegisterForRemoteNotificationsWithError:")
    public void didFailToRegisterForRemoteNotifications(UIApplication application, NSError error) {}
    /**
     * @since Available in iOS 3.0 and later.
     */
    @NotImplemented("application:didReceiveRemoteNotification:")
    public void didReceiveRemoteNotification(UIApplication application, UIRemoteNotification userInfo) {}
    /**
     * @since Available in iOS 4.0 and later.
     */
    @NotImplemented("application:didReceiveLocalNotification:")
    public void didReceiveLocalNotification(UIApplication application, UILocalNotification notification) {}
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:handleActionWithIdentifier:forLocalNotification:completionHandler:")
    public void handleLocalNotificationAction(UIApplication application, String identifier, UILocalNotification notification, @Block Runnable completionHandler) {}
    /**
     * @since Available in iOS 9.0 and later.
     */
    @NotImplemented("application:handleActionWithIdentifier:forRemoteNotification:withResponseInfo:completionHandler:")
    public void handleRemoteNotificationAction(UIApplication application, String identifier, UIRemoteNotification userInfo, NSDictionary<?, ?> responseInfo, @Block Runnable completionHandler) {}
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:handleActionWithIdentifier:forRemoteNotification:completionHandler:")
    public void handleRemoteNotificationAction(UIApplication application, String identifier, UIRemoteNotification userInfo, @Block Runnable completionHandler) {}
    /**
     * @since Available in iOS 9.0 and later.
     */
    @NotImplemented("application:handleActionWithIdentifier:forLocalNotification:withResponseInfo:completionHandler:")
    public void handleLocalNotificationAction(UIApplication application, String identifier, UILocalNotification notification, NSDictionary<?, ?> responseInfo, @Block Runnable completionHandler) {}
    /**
     * @since Available in iOS 7.0 and later.
     */
    @NotImplemented("application:didReceiveRemoteNotification:fetchCompletionHandler:")
    public void didReceiveRemoteNotification(UIApplication application, UIRemoteNotification userInfo, @Block VoidBlock1<UIBackgroundFetchResult> completionHandler) {}
    /**
     * @since Available in iOS 7.0 and later.
     */
    @NotImplemented("application:performFetchWithCompletionHandler:")
    public void performFetch(UIApplication application, @Block VoidBlock1<UIBackgroundFetchResult> completionHandler) {}
    /**
     * @since Available in iOS 7.0 and later.
     */
    @NotImplemented("application:handleEventsForBackgroundURLSession:completionHandler:")
    public void handleEventsForBackgroundURLSession(UIApplication application, String identifier, @Block Runnable completionHandler) {}
    /**
     * @since Available in iOS 8.2 and later.
     */
    @NotImplemented("application:handleWatchKitExtensionRequest:reply:")
    public void handleWatchKitExtensionRequest(UIApplication application, NSDictionary<?, ?> userInfo, @Block VoidBlock1<NSDictionary<?, ?>> reply) {}
    /**
     * @since Available in iOS 9.0 and later.
     */
    @NotImplemented("applicationShouldRequestHealthAuthorization:")
    public void shouldRequestHealthAuthorization(UIApplication application) {}
    /**
     * @since Available in iOS 4.0 and later.
     */
    @NotImplemented("applicationDidEnterBackground:")
    public void didEnterBackground(UIApplication application) {}
    /**
     * @since Available in iOS 4.0 and later.
     */
    @NotImplemented("applicationWillEnterForeground:")
    public void willEnterForeground(UIApplication application) {}
    /**
     * @since Available in iOS 4.0 and later.
     */
    @NotImplemented("applicationProtectedDataWillBecomeUnavailable:")
    public void protectedDataWillBecomeUnavailable(UIApplication application) {}
    /**
     * @since Available in iOS 4.0 and later.
     */
    @NotImplemented("applicationProtectedDataDidBecomeAvailable:")
    public void protectedDataDidBecomeAvailable(UIApplication application) {}
    /**
     * @since Available in iOS 6.0 and later.
     */
    @NotImplemented("application:supportedInterfaceOrientationsForWindow:")
    public UIInterfaceOrientationMask getSupportedInterfaceOrientations(UIApplication application, UIWindow window) { return null; }
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:shouldAllowExtensionPointIdentifier:")
    public boolean shouldAllowExtensionPointIdentifier(UIApplication application, UIApplicationExtensionPointIdentifier extensionPointIdentifier) { return false; }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @NotImplemented("application:viewControllerWithRestorationIdentifierPath:coder:")
    public UIViewController getViewController(UIApplication application, @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> identifierComponents, NSCoder coder) { return null; }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @NotImplemented("application:shouldSaveApplicationState:")
    public boolean shouldSaveApplicationState(UIApplication application, NSCoder coder) { return false; }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @NotImplemented("application:shouldRestoreApplicationState:")
    public boolean shouldRestoreApplicationState(UIApplication application, NSCoder coder) { return false; }
    /**
     * @since Available in iOS 6.0 and later.
     */
    @NotImplemented("application:willEncodeRestorableStateWithCoder:")
    public void willEncodeRestorableState(UIApplication application, NSCoder coder) {}
    /**
     * @since Available in iOS 6.0 and later.
     */
    @NotImplemented("application:didDecodeRestorableStateWithCoder:")
    public void didDecodeRestorableState(UIApplication application, NSCoder coder) {}
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:willContinueUserActivityWithType:")
    public boolean willContinueUserActivity(UIApplication application, String userActivityType) { return false; }
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:continueUserActivity:restorationHandler:")
    public boolean continueUserActivity(UIApplication application, NSUserActivity userActivity, @Block VoidBlock1<NSArray<UIResponder>> restorationHandler) { return false; }
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:didFailToContinueUserActivityWithType:error:")
    public void didFailToContinueUserActivity(UIApplication application, String userActivityType, NSError error) {}
    /**
     * @since Available in iOS 8.0 and later.
     */
    @NotImplemented("application:didUpdateUserActivity:")
    public void didUpdateUserActivity(UIApplication application, NSUserActivity userActivity) {}
    /*</methods>*/
}

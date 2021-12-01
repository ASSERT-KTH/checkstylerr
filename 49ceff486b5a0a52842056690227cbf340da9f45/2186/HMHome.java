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
package com.bugvm.apple.homekit;

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
import com.bugvm.apple.corelocation.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 8.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("HomeKit") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/HMHome/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class HMHomePtr extends Ptr<HMHome, HMHomePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(HMHome.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    protected HMHome(SkipInit skipInit) { super(skipInit); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "delegate")
    public native HMHomeDelegate getDelegate();
    @Property(selector = "setDelegate:", strongRef = true)
    public native void setDelegate(HMHomeDelegate v);
    @Property(selector = "name")
    public native String getName();
    @Property(selector = "isPrimary")
    public native boolean isPrimary();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "uniqueIdentifier")
    public native NSUUID getUniqueIdentifier();
    @Property(selector = "accessories")
    public native NSArray<HMAccessory> getAccessories();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "currentUser")
    public native HMUser getCurrentUser();
    /**
     * @since Available in iOS 8.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Property(selector = "users")
    public native NSArray<HMUser> getUsers();
    @Property(selector = "rooms")
    public native NSArray<HMRoom> getRooms();
    @Property(selector = "zones")
    public native NSArray<HMZone> getZones();
    @Property(selector = "serviceGroups")
    public native NSArray<HMServiceGroup> getServiceGroups();
    @Property(selector = "actionSets")
    public native NSArray<HMActionSet> getActionSets();
    @Property(selector = "triggers")
    public native NSArray<HMTrigger> getTriggers();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "updateName:completionHandler:")
    public native void updateName(String name, @Block VoidBlock1<NSError> completion);
    @Method(selector = "addAccessory:completionHandler:")
    public native void addAccessory(HMAccessory accessory, @Block VoidBlock1<NSError> completion);
    @Method(selector = "removeAccessory:completionHandler:")
    public native void removeAccessory(HMAccessory accessory, @Block VoidBlock1<NSError> completion);
    @Method(selector = "assignAccessory:toRoom:completionHandler:")
    public native void assignAccessoryToRoom(HMAccessory accessory, HMRoom room, @Block VoidBlock1<NSError> completion);
    @Method(selector = "servicesWithTypes:")
    public native NSArray<HMService> getServicesWithTypes(@com.bugvm.rt.bro.annotation.Marshaler(HMServiceType.AsListMarshaler.class) List<HMServiceType> serviceTypes);
    @Method(selector = "unblockAccessory:completionHandler:")
    public native void unblockAccessory(HMAccessory accessory, @Block VoidBlock1<NSError> completion);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "manageUsersWithCompletionHandler:")
    public native void manageUsers(@Block VoidBlock1<NSError> completion);
    /**
     * @since Available in iOS 8.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Method(selector = "addUserWithCompletionHandler:")
    public native void addUser(@Block VoidBlock2<HMUser, NSError> completion);
    /**
     * @since Available in iOS 8.0 and later.
     * @deprecated Deprecated in iOS 9.0.
     */
    @Deprecated
    @Method(selector = "removeUser:completionHandler:")
    public native void removeUser(HMUser user, @Block VoidBlock1<NSError> completion);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "homeAccessControlForUser:")
    public native HMHomeAccessControl getHomeAccessControlForUser(HMUser user);
    @Method(selector = "addRoomWithName:completionHandler:")
    public native void addRoom(String roomName, @Block VoidBlock2<HMRoom, NSError> completion);
    @Method(selector = "removeRoom:completionHandler:")
    public native void removeRoom(HMRoom room, @Block VoidBlock1<NSError> completion);
    @Method(selector = "roomForEntireHome")
    public native HMRoom getRoomForEntireHome();
    @Method(selector = "addZoneWithName:completionHandler:")
    public native void addZone(String zoneName, @Block VoidBlock2<HMZone, NSError> completion);
    @Method(selector = "removeZone:completionHandler:")
    public native void removeZone(HMZone zone, @Block VoidBlock1<NSError> completion);
    @Method(selector = "addServiceGroupWithName:completionHandler:")
    public native void addServiceGroup(String serviceGroupName, @Block VoidBlock2<HMServiceGroup, NSError> completion);
    @Method(selector = "removeServiceGroup:completionHandler:")
    public native void removeServiceGroup(HMServiceGroup group, @Block VoidBlock1<NSError> completion);
    @Method(selector = "addActionSetWithName:completionHandler:")
    public native void addActionSet(String actionSetName, @Block VoidBlock2<HMActionSet, NSError> completion);
    @Method(selector = "removeActionSet:completionHandler:")
    public native void removeActionSet(HMActionSet actionSet, @Block VoidBlock1<NSError> completion);
    @Method(selector = "executeActionSet:completionHandler:")
    public native void executeActionSet(HMActionSet actionSet, @Block VoidBlock1<NSError> completion);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "builtinActionSetOfType:")
    public native HMActionSet getBuiltinActionSet(HMActionSetType actionSetType);
    @Method(selector = "addTrigger:completionHandler:")
    public native void addTrigger(HMTrigger trigger, @Block VoidBlock1<NSError> completion);
    @Method(selector = "removeTrigger:completionHandler:")
    public native void removeTrigger(HMTrigger trigger, @Block VoidBlock1<NSError> completion);
    /*</methods>*/
}

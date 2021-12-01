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
package com.bugvm.apple.coredata;

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
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 3.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("CoreData") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSEntityDescription/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements NSCoding, NSFastEnumeration/*</implements>*/ {

    /*<ptr>*/public static class NSEntityDescriptionPtr extends Ptr<NSEntityDescription, NSEntityDescriptionPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSEntityDescription.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSEntityDescription() {}
    protected NSEntityDescription(SkipInit skipInit) { super(skipInit); }
    public NSEntityDescription(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "managedObjectModel")
    public native NSManagedObjectModel getManagedObjectModel();
    @Property(selector = "managedObjectClassName")
    public native String getManagedObjectClassName();
    @Property(selector = "setManagedObjectClassName:")
    public native void setManagedObjectClassName(String v);
    @Property(selector = "name")
    public native String getName();
    @Property(selector = "setName:")
    public native void setName(String v);
    @Property(selector = "isAbstract")
    public native boolean isAbstract();
    @Property(selector = "setAbstract:")
    public native void setAbstract(boolean v);
    @Property(selector = "subentitiesByName")
    public native NSDictionary<NSString, NSEntityDescription> getSubentitiesByName();
    @Property(selector = "subentities")
    public native NSArray<NSEntityDescription> getSubentities();
    @Property(selector = "setSubentities:")
    public native void setSubentities(NSArray<NSEntityDescription> v);
    @Property(selector = "superentity")
    public native NSEntityDescription getSuperentity();
    @Property(selector = "propertiesByName")
    public native NSDictionary<NSString, NSPropertyDescription> getPropertiesByName();
    @Property(selector = "properties")
    public native NSArray<NSPropertyDescription> getProperties();
    @Property(selector = "setProperties:")
    public native void setProperties(NSArray<NSPropertyDescription> v);
    @Property(selector = "userInfo")
    public native NSDictionary<?, ?> getUserInfo();
    @Property(selector = "setUserInfo:")
    public native void setUserInfo(NSDictionary<?, ?> v);
    @Property(selector = "attributesByName")
    public native NSDictionary<NSString, NSAttributeDescription> getAttributesByName();
    @Property(selector = "relationshipsByName")
    public native NSDictionary<NSString, NSRelationshipDescription> getRelationshipsByName();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "versionHash")
    public native NSData getVersionHash();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "versionHashModifier")
    public native String getVersionHashModifier();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "setVersionHashModifier:")
    public native void setVersionHashModifier(String v);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "renamingIdentifier")
    public native String getRenamingIdentifier();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "setRenamingIdentifier:")
    public native void setRenamingIdentifier(String v);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "compoundIndexes")
    public native NSArray<NSArray<?>> getCompoundIndexes();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setCompoundIndexes:")
    public native void setCompoundIndexes(NSArray<NSArray<?>> v);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "uniquenessConstraints")
    public native NSArray<NSArray<?>> getUniquenessConstraints();
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Property(selector = "setUniquenessConstraints:")
    public native void setUniquenessConstraints(NSArray<NSArray<?>> v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "relationshipsWithDestinationEntity:")
    public native NSArray<NSRelationshipDescription> getRelationshipsWithDestination(NSEntityDescription entity);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Method(selector = "isKindOfEntity:")
    public native boolean isKindOfEntity(NSEntityDescription entity);
    @Method(selector = "entityForName:inManagedObjectContext:")
    public static native NSEntityDescription getEntityByNameInContext(String entityName, NSManagedObjectContext context);
    @Method(selector = "insertNewObjectForEntityForName:inManagedObjectContext:")
    public static native NSEntityDescription insertNewEntityInContext(String entityName, NSManagedObjectContext context);
    @Method(selector = "encodeWithCoder:")
    public native void encode(NSCoder coder);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    /*</methods>*/
}

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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSManagedObjectModel/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*/implements NSCoding, NSFastEnumeration/*</implements>*/ {

    /*<ptr>*/public static class NSManagedObjectModelPtr extends Ptr<NSManagedObjectModel, NSManagedObjectModelPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSManagedObjectModel.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSManagedObjectModel() {}
    protected NSManagedObjectModel(SkipInit skipInit) { super(skipInit); }
    public NSManagedObjectModel(NSURL url) { super((SkipInit) null); initObject(init(url)); }
    public NSManagedObjectModel(NSCoder aDecoder) { super((SkipInit) null); initObject(init(aDecoder)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "entitiesByName")
    public native NSDictionary<NSString, NSEntityDescription> getEntitiesByName();
    @Property(selector = "entities")
    public native NSArray<NSEntityDescription> getEntities();
    @Property(selector = "setEntities:")
    public native void setEntities(NSArray<NSEntityDescription> v);
    @Property(selector = "configurations")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> getConfigurations();
    @Property(selector = "localizationDictionary")
    public native NSDictionary<NSString, NSString> getLocalizationDictionary();
    @Property(selector = "setLocalizationDictionary:")
    public native void setLocalizationDictionary(NSDictionary<NSString, NSString> v);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "fetchRequestTemplatesByName")
    public native NSDictionary<NSString, NSFetchRequest> getFetchRequestTemplatesByName();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "versionIdentifiers")
    public native NSSet<?> getVersionIdentifiers();
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "setVersionIdentifiers:")
    public native void setVersionIdentifiers(NSSet<?> v);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Property(selector = "entityVersionHashesByName")
    public native NSDictionary<?, ?> getEntityVersionHashesByName();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "initWithContentsOfURL:")
    protected native @Pointer long init(NSURL url);
    @Method(selector = "entitiesForConfiguration:")
    public native NSArray<NSEntityDescription> getEntitiesForConfiguration(String configuration);
    @Method(selector = "setEntities:forConfiguration:")
    public native void setEntitiesForConfiguration(NSArray<NSEntityDescription> entities, String configuration);
    @Method(selector = "setFetchRequestTemplate:forName:")
    public native void setFetchRequestTemplate(NSFetchRequest fetchRequestTemplate, String name);
    @Method(selector = "fetchRequestTemplateForName:")
    public native NSFetchRequest getFetchRequestTemplate(String name);
    @Method(selector = "fetchRequestFromTemplateWithName:substitutionVariables:")
    public native NSFetchRequest getFetchRequestTemplate(String name, NSDictionary<NSString, ?> variables);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Method(selector = "isConfiguration:compatibleWithStoreMetadata:")
    public native boolean isConfigurationCompatibleWithStoreMetadata(String configuration, NSPersistentStoreMetadata metadata);
    @Method(selector = "mergedModelFromBundles:")
    public static native NSManagedObjectModel createFromBundles(NSArray<NSBundle> bundles);
    @Method(selector = "modelByMergingModels:")
    public static native NSManagedObjectModel createByMergingModels(NSArray<NSManagedObjectModel> models);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Method(selector = "mergedModelFromBundles:forStoreMetadata:")
    public static native NSManagedObjectModel createFromBundles(NSArray<NSBundle> bundles, NSPersistentStoreMetadata metadata);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Method(selector = "modelByMergingModels:forStoreMetadata:")
    public static native NSManagedObjectModel createByMergingModels(NSArray<NSManagedObjectModel> models, NSPersistentStoreMetadata metadata);
    @Method(selector = "encodeWithCoder:")
    public native void encode(NSCoder coder);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder aDecoder);
    /*</methods>*/
}

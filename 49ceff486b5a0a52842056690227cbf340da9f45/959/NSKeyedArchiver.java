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

/*</javadoc>*/
/*<annotations>*/@Library("Foundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSKeyedArchiver/*</name>*/ 
    extends /*<extends>*/NSCoder/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class NSKeyedArchiverPtr extends Ptr<NSKeyedArchiver, NSKeyedArchiverPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSKeyedArchiver.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSKeyedArchiver() {}
    protected NSKeyedArchiver(SkipInit skipInit) { super(skipInit); }
    public NSKeyedArchiver(NSMutableData data) { super((SkipInit) null); initObject(init(data)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "delegate")
    public native NSKeyedArchiverDelegate getDelegate();
    @Property(selector = "setDelegate:", strongRef = true)
    public native void setDelegate(NSKeyedArchiverDelegate v);
    @Property(selector = "outputFormat")
    public native NSPropertyListFormat getOutputFormat();
    @Property(selector = "setOutputFormat:")
    public native void setOutputFormat(NSPropertyListFormat v);
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "requiresSecureCoding")
    public native boolean requiresSecureCoding();
    /**
     * @since Available in iOS 6.0 and later.
     */
    @Property(selector = "setRequiresSecureCoding:")
    public native void setRequiresSecureCoding(boolean v);
    /*</properties>*/
    /*<members>*//*</members>*/
    
    public boolean archive(NSObject rootObject, File file) {
        if (rootObject == null) {
            throw new NullPointerException("rootObject");
        }
        if (file == null) {
            throw new NullPointerException("file");
        }
        return archiveRootObject(rootObject, file.getAbsolutePath());
    }
    
    /*<methods>*/
    /**
     * @since Available in iOS 7.0 and later.
     */
    @GlobalValue(symbol="NSKeyedArchiveRootObjectKey", optional=true)
    public static native String ArchiveRootObjectKey();
    
    @Method(selector = "initForWritingWithMutableData:")
    protected native @Pointer long init(NSMutableData data);
    @Method(selector = "finishEncoding")
    public native void finishEncoding();
    @Method(selector = "setClassName:forClass:")
    public native void setClassNameForClass(String codedName, Class<? extends NSObject> cls);
    @Method(selector = "classNameForClass:")
    public native String getClassNameForClass(Class<? extends NSObject> cls);
    @Method(selector = "archivedDataWithRootObject:")
    public static native NSData archive(NSObject rootObject);
    @Method(selector = "archiveRootObject:toFile:")
    private static native boolean archiveRootObject(NSObject rootObject, String path);
    @Method(selector = "setClassName:forClass:")
    public static native void setDefaultClassNameForClass(String codedName, Class<? extends NSObject> cls);
    @Method(selector = "classNameForClass:")
    public static native String getDefaultClassNameForClass(Class<? extends NSObject> cls);
    /*</methods>*/
}

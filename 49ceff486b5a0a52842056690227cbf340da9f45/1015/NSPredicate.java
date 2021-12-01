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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSPredicate/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class NSPredicatePtr extends Ptr<NSPredicate, NSPredicatePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSPredicate.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSPredicate() {}
    protected NSPredicate(SkipInit skipInit) { super(skipInit); }
    public NSPredicate(String predicateFormat, NSArray<?> arguments) { super(create(predicateFormat, arguments)); retain(getHandle()); }
    public NSPredicate(boolean value) { super(create(value)); retain(getHandle()); }
    /**
     * @since Available in iOS 4.0 and later.
     */
    public NSPredicate(@Block Block2<NSObject, NSDictionary<NSString, ?>, Boolean> block) { super(create(block)); retain(getHandle()); }
    /*</constructors>*/
    public NSPredicate(String predicateFormat, Object... arguments) {
        super(create(predicateFormat, arguments));
    }
    public NSPredicate(String predicateFormat, NSObject ... arguments) {
        super(create(predicateFormat, new NSArray<NSObject>(arguments)));
    }
    
    private static long create(String predicateFormat, Object... arguments) {
        NSArray<NSObject> args = new NSMutableArray<>();
        int i = 0;
        for (Object o : arguments) {
            if (o instanceof Number) {
                args.add(NSNumber.valueOf((Number) o));
            } else if (o instanceof String) {
                args.add(new NSString((String) o));
            } else if (o instanceof NSPredicateKeyPath) {
                args.add(((NSPredicateKeyPath) o).value());
            } else if (o instanceof Collection) {
                Collection<?> c = (Collection<?>) o;
                NSArray<NSString> a = new NSMutableArray<>();
                for (Object e : c) {
                    a.add(new NSString(e.toString()));
                }
                args.add(a);
            } else if (o instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) o;
                NSDictionary<NSString, NSString> d = new NSMutableDictionary<>();
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    d.put(new NSString(e.getKey().toString()), new NSString(e.getValue().toString()));
                }
                args.add(d);
            } else if (o instanceof NSObject) {
               args.add((NSObject) o); 
            } else if (o == null) {
                throw new IllegalArgumentException("argument " + i + " cannot be null!");
            } else {
                throw new IllegalArgumentException("type of argument " + i + " not supported: " + o.getClass());
            }
            i++;
        }
        
        return create(predicateFormat, args);
    }
    /*<properties>*/
    @Property(selector = "predicateFormat")
    public native String getPredicateFormat();
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @Method(selector = "predicateWithSubstitutionVariables:")
    public native NSPredicate newPredicate(NSDictionary<NSString, ?> variables);
    @Method(selector = "evaluateWithObject:")
    public native boolean evaluate(NSObject object);
    /**
     * @since Available in iOS 3.0 and later.
     */
    @Method(selector = "evaluateWithObject:substitutionVariables:")
    public native boolean evaluate(NSObject object, NSDictionary<NSString, ?> variables);
    /**
     * @since Available in iOS 7.0 and later.
     */
    @Method(selector = "allowEvaluation")
    public native void allowEvaluation();
    @Method(selector = "predicateWithFormat:argumentArray:")
    protected static native @Pointer long create(String predicateFormat, NSArray<?> arguments);
    @Method(selector = "predicateWithValue:")
    protected static native @Pointer long create(boolean value);
    /**
     * @since Available in iOS 4.0 and later.
     */
    @Method(selector = "predicateWithBlock:")
    protected static native @Pointer long create(@Block Block2<NSObject, NSDictionary<NSString, ?>, Boolean> block);
    /*</methods>*/
}

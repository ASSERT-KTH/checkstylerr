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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NSFileHandle/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    public static class Notifications {
        public static NSObject observeReadCompletion(NSFileHandle object, final VoidBlock2<NSFileHandle, NSData> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(ReadCompletionNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    NSData d = null;
                    NSDictionary<?, ?> data = a.getUserInfo();
                    if (data.containsKey(NotificationDataItem())) {
                        d = (NSData)data.get(NotificationDataItem());
                    }
                    block.invoke((NSFileHandle)a.getObject(), d);
                }
            });
        }
        public static NSObject observeReadToEndOfFileCompletion(NSFileHandle object, final VoidBlock2<NSFileHandle, NSData> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(ReadToEndOfFileCompletionNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    NSData d = null;
                    NSDictionary<?, ?> data = a.getUserInfo();
                    if (data.containsKey(NotificationDataItem())) {
                        d = (NSData)data.get(NotificationDataItem());
                    }
                    block.invoke((NSFileHandle)a.getObject(), d);
                }
            });
        }
        public static NSObject observeConnectionAccepted(NSFileHandle object, final VoidBlock2<NSFileHandle, NSFileHandle> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(ConnectionAcceptedNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    NSFileHandle f = null;
                    NSDictionary<?, ?> data = a.getUserInfo();
                    if (data.containsKey(NotificationDataItem())) {
                        f = (NSFileHandle)data.get(NotificationFileHandleItem());
                    }
                    block.invoke((NSFileHandle)a.getObject(), f);
                }
            });
        }
        public static NSObject observeDataAvailable(NSFileHandle object, final VoidBlock1<NSFileHandle> block) {
            return NSNotificationCenter.getDefaultCenter().addObserver(DataAvailableNotification(), object, NSOperationQueue.getMainQueue(), new VoidBlock1<NSNotification>() {
                @Override
                public void invoke(NSNotification a) {
                    block.invoke((NSFileHandle)a.getObject());
                }
            });
        }
    }
    
    /*<ptr>*/public static class NSFileHandlePtr extends Ptr<NSFileHandle, NSFileHandlePtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(NSFileHandle.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public NSFileHandle() {}
    protected NSFileHandle(SkipInit skipInit) { super(skipInit); }
    public NSFileHandle(int fd, boolean closeopt) { super((SkipInit) null); initObject(init(fd, closeopt)); }
    public NSFileHandle(NSCoder coder) { super((SkipInit) null); initObject(init(coder)); }
    public NSFileHandle(int fd) { super((SkipInit) null); initObject(init(fd)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "availableData")
    public native NSData getAvailableData();
    @Property(selector = "offsetInFile")
    public native long getOffsetInFile();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "readabilityHandler")
    public native @Block VoidBlock1<NSFileHandle> getReadabilityHandler();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setReadabilityHandler:")
    public native void setReadabilityHandler(@Block VoidBlock1<NSFileHandle> v);
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "writeabilityHandler")
    public native @Block VoidBlock1<NSFileHandle> getWriteabilityHandler();
    /**
     * @since Available in iOS 5.0 and later.
     */
    @Property(selector = "setWriteabilityHandler:")
    public native void setWriteabilityHandler(@Block VoidBlock1<NSFileHandle> v);
    @Property(selector = "fileDescriptor")
    public native int getFileDescriptor();
    /*</properties>*/
    /*<members>*//*</members>*/
    
    public void readInBackgroundAndNotify(NSRunLoopMode...modes) {
        List<String> list = new ArrayList<>();
        for (NSRunLoopMode mode : modes) {
            list.add(mode.value().toString());
        }
        readInBackgroundAndNotify(list);
    }
    public void readToEndOfFileInBackgroundAndNotify(NSRunLoopMode...modes) {
        List<String> list = new ArrayList<>();
        for (NSRunLoopMode mode : modes) {
            list.add(mode.value().toString());
        }
        readToEndOfFileInBackgroundAndNotify(list);
    }
    public void acceptConnectionInBackgroundAndNotify(NSRunLoopMode...modes) {
        List<String> list = new ArrayList<>();
        for (NSRunLoopMode mode : modes) {
            list.add(mode.value().toString());
        }
        acceptConnectionInBackgroundAndNotify(list);
    }
    public void waitForDataInBackgroundAndNotify(NSRunLoopMode...modes) {
        List<String> list = new ArrayList<>();
        for (NSRunLoopMode mode : modes) {
            list.add(mode.value().toString());
        }
        waitForDataInBackgroundAndNotify(list);
    }
    /*<methods>*/
    @GlobalValue(symbol="NSFileHandleReadCompletionNotification", optional=true)
    public static native NSString ReadCompletionNotification();
    @GlobalValue(symbol="NSFileHandleReadToEndOfFileCompletionNotification", optional=true)
    public static native NSString ReadToEndOfFileCompletionNotification();
    @GlobalValue(symbol="NSFileHandleConnectionAcceptedNotification", optional=true)
    public static native NSString ConnectionAcceptedNotification();
    @GlobalValue(symbol="NSFileHandleDataAvailableNotification", optional=true)
    public static native NSString DataAvailableNotification();
    @GlobalValue(symbol="NSFileHandleNotificationDataItem", optional=true)
    protected static native NSString NotificationDataItem();
    @GlobalValue(symbol="NSFileHandleNotificationFileHandleItem", optional=true)
    protected static native NSString NotificationFileHandleItem();
    
    @Method(selector = "readDataToEndOfFile")
    public native NSData readDataToEndOfFile();
    @Method(selector = "readDataOfLength:")
    public native NSData readData(@MachineSizedUInt long length);
    @Method(selector = "writeData:")
    public native void writeData(NSData data);
    @Method(selector = "seekToEndOfFile")
    public native long seekToEndOfFile();
    @Method(selector = "seekToFileOffset:")
    public native void seekToFileOffset(long offset);
    @Method(selector = "truncateFileAtOffset:")
    public native void truncateFile(long offset);
    @Method(selector = "synchronizeFile")
    public native void synchronizeFile();
    @Method(selector = "closeFile")
    public native void closeFile();
    @Method(selector = "initWithFileDescriptor:closeOnDealloc:")
    protected native @Pointer long init(int fd, boolean closeopt);
    @Method(selector = "initWithCoder:")
    protected native @Pointer long init(NSCoder coder);
    @Method(selector = "fileHandleWithStandardInput")
    public static native NSFileHandle getStandardInput();
    @Method(selector = "fileHandleWithStandardOutput")
    public static native NSFileHandle getStandardOutput();
    @Method(selector = "fileHandleWithStandardError")
    public static native NSFileHandle getStandardError();
    @Method(selector = "fileHandleWithNullDevice")
    public static native NSFileHandle getNullDevice();
    @Method(selector = "fileHandleForReadingAtPath:")
    public static native NSFileHandle createForReading(String path);
    @Method(selector = "fileHandleForWritingAtPath:")
    public static native NSFileHandle createForWriting(String path);
    @Method(selector = "fileHandleForUpdatingAtPath:")
    public static native NSFileHandle createForUpdating(String path);
    /**
     * @since Available in iOS 4.0 and later.
     */
    public static NSFileHandle createForReading(NSURL url) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSFileHandle result = createForReading(url, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @Method(selector = "fileHandleForReadingFromURL:error:")
    private static native NSFileHandle createForReading(NSURL url, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 4.0 and later.
     */
    public static NSFileHandle createForWriting(NSURL url) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSFileHandle result = createForWriting(url, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @Method(selector = "fileHandleForWritingToURL:error:")
    private static native NSFileHandle createForWriting(NSURL url, NSError.NSErrorPtr error);
    /**
     * @since Available in iOS 4.0 and later.
     */
    public static NSFileHandle createForUpdating(NSURL url) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       NSFileHandle result = createForUpdating(url, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    /**
     * @since Available in iOS 4.0 and later.
     */
    @Method(selector = "fileHandleForUpdatingURL:error:")
    private static native NSFileHandle createForUpdating(NSURL url, NSError.NSErrorPtr error);
    @Method(selector = "readInBackgroundAndNotifyForModes:")
    public native void readInBackgroundAndNotify(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> modes);
    @Method(selector = "readInBackgroundAndNotify")
    public native void readInBackgroundAndNotify();
    @Method(selector = "readToEndOfFileInBackgroundAndNotifyForModes:")
    public native void readToEndOfFileInBackgroundAndNotify(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> modes);
    @Method(selector = "readToEndOfFileInBackgroundAndNotify")
    public native void readToEndOfFileInBackgroundAndNotify();
    @Method(selector = "acceptConnectionInBackgroundAndNotifyForModes:")
    public native void acceptConnectionInBackgroundAndNotify(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> modes);
    @Method(selector = "acceptConnectionInBackgroundAndNotify")
    public native void acceptConnectionInBackgroundAndNotify();
    @Method(selector = "waitForDataInBackgroundAndNotifyForModes:")
    public native void waitForDataInBackgroundAndNotify(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsStringListMarshaler.class) List<String> modes);
    @Method(selector = "waitForDataInBackgroundAndNotify")
    public native void waitForDataInBackgroundAndNotify();
    @Method(selector = "initWithFileDescriptor:")
    protected native @Pointer long init(int fd);
    /*</methods>*/
}

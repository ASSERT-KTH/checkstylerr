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
package com.bugvm.apple.avfoundation;

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
import com.bugvm.apple.corefoundation.*;
import com.bugvm.apple.dispatch.*;
import com.bugvm.apple.coreanimation.*;
import com.bugvm.apple.coreimage.*;
import com.bugvm.apple.coregraphics.*;
import com.bugvm.apple.coreaudio.*;
import com.bugvm.apple.coremedia.*;
import com.bugvm.apple.corevideo.*;
import com.bugvm.apple.mediatoolbox.*;
import com.bugvm.apple.audiotoolbox.*;
import com.bugvm.apple.audiounit.*;
/*</imports>*/

/*<javadoc>*/
/**
 * @since Available in iOS 9.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("AVFoundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVAudioConverter/*</name>*/ 
    extends /*<extends>*/NSObject/*</extends>*/ 
    /*<implements>*//*</implements>*/ {

    /*<ptr>*/public static class AVAudioConverterPtr extends Ptr<AVAudioConverter, AVAudioConverterPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(AVAudioConverter.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public AVAudioConverter() {}
    protected AVAudioConverter(SkipInit skipInit) { super(skipInit); }
    public AVAudioConverter(AVAudioFormat fromFormat, AVAudioFormat toFormat) { super((SkipInit) null); initObject(init(fromFormat, toFormat)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "inputFormat")
    public native AVAudioFormat getInputFormat();
    @Property(selector = "outputFormat")
    public native AVAudioFormat getOutputFormat();
    @Property(selector = "channelMap")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsIntegerListMarshaler.class) List<Integer> getChannelMap();
    @Property(selector = "setChannelMap:")
    public native void setChannelMap(@com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsIntegerListMarshaler.class) List<Integer> v);
    @Property(selector = "magicCookie")
    public native NSData getMagicCookie();
    @Property(selector = "setMagicCookie:")
    public native void setMagicCookie(NSData v);
    @Property(selector = "downmix")
    public native boolean isDownmix();
    @Property(selector = "setDownmix:")
    public native void setDownmix(boolean v);
    @Property(selector = "dither")
    public native boolean isDither();
    @Property(selector = "setDither:")
    public native void setDither(boolean v);
    @Property(selector = "sampleRateConverterQuality")
    public native AVAudioQuality getSampleRateConverterQuality();
    @Property(selector = "setSampleRateConverterQuality:")
    public native void setSampleRateConverterQuality(AVAudioQuality v);
    @Property(selector = "sampleRateConverterAlgorithm")
    public native AVSampleRateConverterAlgorithm getSampleRateConverterAlgorithm();
    @Property(selector = "setSampleRateConverterAlgorithm:")
    public native void setSampleRateConverterAlgorithm(AVSampleRateConverterAlgorithm v);
    @Property(selector = "primeMethod")
    public native AVAudioConverterPrimeMethod getPrimeMethod();
    @Property(selector = "setPrimeMethod:")
    public native void setPrimeMethod(AVAudioConverterPrimeMethod v);
    @Property(selector = "primeInfo")
    public native @ByVal AVAudioConverterPrimeInfo getPrimeInfo();
    @Property(selector = "setPrimeInfo:")
    public native void setPrimeInfo(@ByVal AVAudioConverterPrimeInfo v);
    @Property(selector = "bitRate")
    public native @MachineSizedSInt long getBitRate();
    @Property(selector = "setBitRate:")
    public native void setBitRate(@MachineSizedSInt long v);
    @Property(selector = "bitRateStrategy")
    public native AVAudioBitRateStrategy getBitRateStrategy();
    @Property(selector = "setBitRateStrategy:")
    public native void setBitRateStrategy(AVAudioBitRateStrategy v);
    @Property(selector = "maximumOutputPacketSize")
    public native @MachineSizedSInt long getMaximumOutputPacketSize();
    @Property(selector = "availableEncodeBitRates")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsIntegerListMarshaler.class) List<Integer> getAvailableEncodeBitRates();
    @Property(selector = "applicableEncodeBitRates")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsIntegerListMarshaler.class) List<Integer> getApplicableEncodeBitRates();
    @Property(selector = "availableEncodeSampleRates")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsIntegerListMarshaler.class) List<Integer> getAvailableEncodeSampleRates();
    @Property(selector = "applicableEncodeSampleRates")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsIntegerListMarshaler.class) List<Integer> getApplicableEncodeSampleRates();
    @Property(selector = "availableEncodeChannelLayoutTags")
    public native @com.bugvm.rt.bro.annotation.Marshaler(NSArray.AsIntegerListMarshaler.class) List<Integer> getAvailableEncodeChannelLayoutTags();
    /*</properties>*/
    /*<members>*//*</members>*/
    public AVAudioConverterOutputStatus convert(AVAudioBuffer outputBuffer, Block2<Integer, MachineSizedSIntPtr, AVAudioBuffer> inputBlock) throws NSErrorException {
        NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
        AVAudioConverterOutputStatus result = convert(outputBuffer, ptr, inputBlock);
        if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
        return result;
    } // TODO simplify the inputBlock!!!
    /*<methods>*/
    @Method(selector = "initFromFormat:toFormat:")
    protected native @Pointer long init(AVAudioFormat fromFormat, AVAudioFormat toFormat);
    @Method(selector = "reset")
    public native void reset();
    public boolean convert(AVAudioPCMBuffer outputBuffer, AVAudioPCMBuffer inputBuffer) throws NSErrorException {
       NSError.NSErrorPtr ptr = new NSError.NSErrorPtr();
       boolean result = convert(outputBuffer, inputBuffer, ptr);
       if (ptr.get() != null) { throw new NSErrorException(ptr.get()); }
       return result;
    }
    @Method(selector = "convertToBuffer:fromBuffer:error:")
    private native boolean convert(AVAudioPCMBuffer outputBuffer, AVAudioPCMBuffer inputBuffer, NSError.NSErrorPtr outError);
    @Method(selector = "convertToBuffer:error:withInputFromBlock:")
    protected native AVAudioConverterOutputStatus convert(AVAudioBuffer outputBuffer, NSError.NSErrorPtr outError, @Block Block2<Integer, MachineSizedSIntPtr, AVAudioBuffer> inputBlock);
    /*</methods>*/
}

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
 * @since Available in iOS 8.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Library("AVFoundation") @NativeClass/*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/AVAudioUnitMIDIInstrument/*</name>*/ 
    extends /*<extends>*/AVAudioUnit/*</extends>*/ 
    /*<implements>*/implements AVAudioMixing/*</implements>*/ {

    /*<ptr>*/public static class AVAudioUnitMIDIInstrumentPtr extends Ptr<AVAudioUnitMIDIInstrument, AVAudioUnitMIDIInstrumentPtr> {}/*</ptr>*/
    /*<bind>*/static { ObjCRuntime.bind(AVAudioUnitMIDIInstrument.class); }/*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*/
    public AVAudioUnitMIDIInstrument() {}
    protected AVAudioUnitMIDIInstrument(SkipInit skipInit) { super(skipInit); }
    @WeaklyLinked
    public AVAudioUnitMIDIInstrument(@ByVal AudioComponentDescription description) { super((SkipInit) null); initObject(init(description)); }
    /*</constructors>*/
    /*<properties>*/
    @Property(selector = "volume")
    public native float getVolume();
    @Property(selector = "setVolume:")
    public native void setVolume(float v);
    @Property(selector = "pan")
    public native float getPan();
    @Property(selector = "setPan:")
    public native void setPan(float v);
    @Property(selector = "renderingAlgorithm")
    public native AVAudio3DMixingRenderingAlgorithm getRenderingAlgorithm();
    @Property(selector = "setRenderingAlgorithm:")
    public native void setRenderingAlgorithm(AVAudio3DMixingRenderingAlgorithm v);
    @Property(selector = "rate")
    public native float getRate();
    @Property(selector = "setRate:")
    public native void setRate(float v);
    @Property(selector = "reverbBlend")
    public native float getReverbBlend();
    @Property(selector = "setReverbBlend:")
    public native void setReverbBlend(float v);
    @Property(selector = "obstruction")
    public native float getObstruction();
    @Property(selector = "setObstruction:")
    public native void setObstruction(float v);
    @Property(selector = "occlusion")
    public native float getOcclusion();
    @Property(selector = "setOcclusion:")
    public native void setOcclusion(float v);
    @Property(selector = "position")
    public native @ByVal AVAudio3DPoint getPosition();
    @Property(selector = "setPosition:")
    public native void setPosition(@ByVal AVAudio3DPoint v);
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    @WeaklyLinked
    @Method(selector = "initWithAudioComponentDescription:")
    protected native @Pointer long init(@ByVal AudioComponentDescription description);
    @Method(selector = "startNote:withVelocity:onChannel:")
    public native void startNote(byte note, byte velocity, byte channel);
    @Method(selector = "stopNote:onChannel:")
    public native void stopNote(byte note, byte channel);
    @Method(selector = "sendController:withValue:onChannel:")
    public native void sendController(byte controller, byte value, byte channel);
    @Method(selector = "sendPitchBend:onChannel:")
    public native void sendPitchBend(short pitchbend, byte channel);
    @Method(selector = "sendPressure:onChannel:")
    public native void sendPressure(byte pressure, byte channel);
    @Method(selector = "sendPressureForKey:withValue:onChannel:")
    public native void sendPressure(byte key, byte value, byte channel);
    @Method(selector = "sendProgramChange:onChannel:")
    public native void sendProgramChange(byte program, byte channel);
    @Method(selector = "sendProgramChange:bankMSB:bankLSB:onChannel:")
    public native void sendProgramChange(byte program, byte bankMSB, byte bankLSB, byte channel);
    @Method(selector = "sendMIDIEvent:data1:data2:")
    public native void sendMIDIEvent(byte midiStatus, byte data1, byte data2);
    @Method(selector = "sendMIDIEvent:data1:")
    public native void sendMIDIEvent(byte midiStatus, byte data1);
    @Method(selector = "sendMIDISysExEvent:")
    public native void sendMIDISysExEvent(NSData midiData);
    /**
     * @since Available in iOS 9.0 and later.
     */
    @Method(selector = "destinationForMixer:bus:")
    public native AVAudioMixingDestination getDestinationForMixer(AVAudioNode mixer, @MachineSizedUInt long bus);
    /*</methods>*/
}

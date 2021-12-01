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
 * @since Available in iOS 6.0 and later.
 */
/*</javadoc>*/
/*<annotations>*/@Marshaler(Bits.AsMachineSizedIntMarshaler.class)/*</annotations>*/
public final class /*<name>*/AVAudioSessionCategoryOptions/*</name>*/ extends Bits</*<name>*/AVAudioSessionCategoryOptions/*</name>*/> {
    /*<values>*/
    public static final AVAudioSessionCategoryOptions None = new AVAudioSessionCategoryOptions(0L);
    public static final AVAudioSessionCategoryOptions MixWithOthers = new AVAudioSessionCategoryOptions(1L);
    public static final AVAudioSessionCategoryOptions DuckOthers = new AVAudioSessionCategoryOptions(2L);
    public static final AVAudioSessionCategoryOptions AllowBluetooth = new AVAudioSessionCategoryOptions(4L);
    public static final AVAudioSessionCategoryOptions DefaultToSpeaker = new AVAudioSessionCategoryOptions(8L);
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final AVAudioSessionCategoryOptions InterruptSpokenAudioAndMixWithOthers = new AVAudioSessionCategoryOptions(17L);
    /*</values>*/

    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<methods>*//*</methods>*/

    private static final /*<name>*/AVAudioSessionCategoryOptions/*</name>*/[] values = _values(/*<name>*/AVAudioSessionCategoryOptions/*</name>*/.class);

    public /*<name>*/AVAudioSessionCategoryOptions/*</name>*/(long value) { super(value); }
    private /*<name>*/AVAudioSessionCategoryOptions/*</name>*/(long value, long mask) { super(value, mask); }
    protected /*<name>*/AVAudioSessionCategoryOptions/*</name>*/ wrap(long value, long mask) {
        return new /*<name>*/AVAudioSessionCategoryOptions/*</name>*/(value, mask);
    }
    protected /*<name>*/AVAudioSessionCategoryOptions/*</name>*/[] _values() {
        return values;
    }
    public static /*<name>*/AVAudioSessionCategoryOptions/*</name>*/[] values() {
        return values.clone();
    }
}

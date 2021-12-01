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
/*</javadoc>*/
/*<annotations>*/@Library("HomeKit") @StronglyLinked/*</annotations>*/
@Marshaler(/*<name>*/HMCharacteristicType/*</name>*/.Marshaler.class)
/*<visibility>*/public/*</visibility>*/ class /*<name>*/HMCharacteristicType/*</name>*/ 
    extends /*<extends>*/GlobalValueEnumeration<NSString>/*</extends>*/
    /*<implements>*//*</implements>*/ {

    static { Bro.bind(/*<name>*/HMCharacteristicType/*</name>*/.class); }

    /*<marshalers>*/
    public static class Marshaler {
        @MarshalsPointer
        public static HMCharacteristicType toObject(Class<HMCharacteristicType> cls, long handle, long flags) {
            NSString o = (NSString) NSObject.Marshaler.toObject(NSString.class, handle, flags);
            if (o == null) {
                return null;
            }
            return HMCharacteristicType.valueOf(o);
        }
        @MarshalsPointer
        public static long toNative(HMCharacteristicType o, long flags) {
            if (o == null) {
                return 0L;
            }
            return NSObject.Marshaler.toNative(o.value(), flags);
        }
    }
    public static class AsListMarshaler {
        @SuppressWarnings("unchecked")
        @MarshalsPointer
        public static List<HMCharacteristicType> toObject(Class<? extends NSObject> cls, long handle, long flags) {
            NSArray<NSString> o = (NSArray<NSString>) NSObject.Marshaler.toObject(NSArray.class, handle, flags);
            if (o == null) {
                return null;
            }
            List<HMCharacteristicType> list = new ArrayList<>();
            for (int i = 0; i < o.size(); i++) {
                list.add(HMCharacteristicType.valueOf(o.get(i)));
            }
            return list;
        }
        @MarshalsPointer
        public static long toNative(List<HMCharacteristicType> l, long flags) {
            if (l == null) {
                return 0L;
            }
            NSArray<NSString> array = new NSMutableArray<>();
            for (HMCharacteristicType o : l) {
                array.add(o.value());
            }
            return NSObject.Marshaler.toNative(array, flags);
        }
    }
    /*</marshalers>*/

    /*<constants>*/
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType PowerState = new HMCharacteristicType("PowerState");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Hue = new HMCharacteristicType("Hue");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Saturation = new HMCharacteristicType("Saturation");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Brightness = new HMCharacteristicType("Brightness");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType TemperatureUnits = new HMCharacteristicType("TemperatureUnits");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType CurrentTemperature = new HMCharacteristicType("CurrentTemperature");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType TargetTemperature = new HMCharacteristicType("TargetTemperature");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType CurrentHeatingCooling = new HMCharacteristicType("CurrentHeatingCooling");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType TargetHeatingCooling = new HMCharacteristicType("TargetHeatingCooling");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType CoolingThreshold = new HMCharacteristicType("CoolingThreshold");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType HeatingThreshold = new HMCharacteristicType("HeatingThreshold");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType CurrentRelativeHumidity = new HMCharacteristicType("CurrentRelativeHumidity");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType TargetRelativeHumidity = new HMCharacteristicType("TargetRelativeHumidity");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType CurrentDoorState = new HMCharacteristicType("CurrentDoorState");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType TargetDoorState = new HMCharacteristicType("TargetDoorState");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType ObstructionDetected = new HMCharacteristicType("ObstructionDetected");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Name = new HMCharacteristicType("Name");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Manufacturer = new HMCharacteristicType("Manufacturer");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Model = new HMCharacteristicType("Model");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType SerialNumber = new HMCharacteristicType("SerialNumber");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Identify = new HMCharacteristicType("Identify");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType RotationDirection = new HMCharacteristicType("RotationDirection");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType RotationSpeed = new HMCharacteristicType("RotationSpeed");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType OutletInUse = new HMCharacteristicType("OutletInUse");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Version = new HMCharacteristicType("Version");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType Logs = new HMCharacteristicType("Logs");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType AudioFeedback = new HMCharacteristicType("AudioFeedback");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType AdminOnlyAccess = new HMCharacteristicType("AdminOnlyAccess");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType SecuritySystemAlarmType = new HMCharacteristicType("SecuritySystemAlarmType");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType MotionDetected = new HMCharacteristicType("MotionDetected");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType CurrentLockMechanismState = new HMCharacteristicType("CurrentLockMechanismState");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType TargetLockMechanismState = new HMCharacteristicType("TargetLockMechanismState");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType LockMechanismLastKnownAction = new HMCharacteristicType("LockMechanismLastKnownAction");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType LockManagementControlPoint = new HMCharacteristicType("LockManagementControlPoint");
    /**
     * @since Available in iOS 8.0 and later.
     */
    public static final HMCharacteristicType LockManagementAutoSecureTimeout = new HMCharacteristicType("LockManagementAutoSecureTimeout");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType AirParticulateDensity = new HMCharacteristicType("AirParticulateDensity");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType AirParticulateSize = new HMCharacteristicType("AirParticulateSize");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType AirQuality = new HMCharacteristicType("AirQuality");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType BatteryLevel = new HMCharacteristicType("BatteryLevel");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CarbonDioxideDetected = new HMCharacteristicType("CarbonDioxideDetected");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CarbonDioxideLevel = new HMCharacteristicType("CarbonDioxideLevel");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CarbonDioxidePeakLevel = new HMCharacteristicType("CarbonDioxidePeakLevel");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CarbonMonoxideDetected = new HMCharacteristicType("CarbonMonoxideDetected");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CarbonMonoxideLevel = new HMCharacteristicType("CarbonMonoxideLevel");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CarbonMonoxidePeakLevel = new HMCharacteristicType("CarbonMonoxidePeakLevel");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType ChargingState = new HMCharacteristicType("ChargingState");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType ContactState = new HMCharacteristicType("ContactState");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CurrentHorizontalTilt = new HMCharacteristicType("CurrentHorizontalTilt");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CurrentLightLevel = new HMCharacteristicType("CurrentLightLevel");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CurrentPosition = new HMCharacteristicType("CurrentPosition");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CurrentSecuritySystemState = new HMCharacteristicType("CurrentSecuritySystemState");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType CurrentVerticalTilt = new HMCharacteristicType("CurrentVerticalTilt");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType FirmwareVersion = new HMCharacteristicType("FirmwareVersion");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType HardwareVersion = new HMCharacteristicType("HardwareVersion");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType HoldPosition = new HMCharacteristicType("HoldPosition");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType InputEvent = new HMCharacteristicType("InputEvent");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType LeakDetected = new HMCharacteristicType("LeakDetected");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType OccupancyDetected = new HMCharacteristicType("OccupancyDetected");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType OutputState = new HMCharacteristicType("OutputState");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType PositionState = new HMCharacteristicType("PositionState");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType SmokeDetected = new HMCharacteristicType("SmokeDetected");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType SoftwareVersion = new HMCharacteristicType("SoftwareVersion");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType StatusActive = new HMCharacteristicType("StatusActive");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType StatusFault = new HMCharacteristicType("StatusFault");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType StatusJammed = new HMCharacteristicType("StatusJammed");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType StatusLowBattery = new HMCharacteristicType("StatusLowBattery");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType StatusTampered = new HMCharacteristicType("StatusTampered");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType TargetHorizontalTilt = new HMCharacteristicType("TargetHorizontalTilt");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType TargetSecuritySystemState = new HMCharacteristicType("TargetSecuritySystemState");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType TargetPosition = new HMCharacteristicType("TargetPosition");
    /**
     * @since Available in iOS 9.0 and later.
     */
    public static final HMCharacteristicType TargetVerticalTilt = new HMCharacteristicType("TargetVerticalTilt");
    /*</constants>*/
    
    private static /*<name>*/HMCharacteristicType/*</name>*/[] values = new /*<name>*/HMCharacteristicType/*</name>*/[] {/*<value_list>*/PowerState, Hue, Saturation, Brightness, TemperatureUnits, CurrentTemperature, TargetTemperature, CurrentHeatingCooling, TargetHeatingCooling, CoolingThreshold, HeatingThreshold, CurrentRelativeHumidity, TargetRelativeHumidity, CurrentDoorState, TargetDoorState, ObstructionDetected, Name, Manufacturer, Model, SerialNumber, Identify, RotationDirection, RotationSpeed, OutletInUse, Version, Logs, AudioFeedback, AdminOnlyAccess, SecuritySystemAlarmType, MotionDetected, CurrentLockMechanismState, TargetLockMechanismState, LockMechanismLastKnownAction, LockManagementControlPoint, LockManagementAutoSecureTimeout, AirParticulateDensity, AirParticulateSize, AirQuality, BatteryLevel, CarbonDioxideDetected, CarbonDioxideLevel, CarbonDioxidePeakLevel, CarbonMonoxideDetected, CarbonMonoxideLevel, CarbonMonoxidePeakLevel, ChargingState, ContactState, CurrentHorizontalTilt, CurrentLightLevel, CurrentPosition, CurrentSecuritySystemState, CurrentVerticalTilt, FirmwareVersion, HardwareVersion, HoldPosition, InputEvent, LeakDetected, OccupancyDetected, OutputState, PositionState, SmokeDetected, SoftwareVersion, StatusActive, StatusFault, StatusJammed, StatusLowBattery, StatusTampered, TargetHorizontalTilt, TargetSecuritySystemState, TargetPosition, TargetVerticalTilt/*</value_list>*/};
    
    /*<name>*/HMCharacteristicType/*</name>*/ (String getterName) {
        super(Values.class, getterName);
    }
    
    public static /*<name>*/HMCharacteristicType/*</name>*/ valueOf(/*<type>*/NSString/*</type>*/ value) {
        for (/*<name>*/HMCharacteristicType/*</name>*/ v : values) {
            if (v.value().equals(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No constant with value " + value + " found in " 
            + /*<name>*/HMCharacteristicType/*</name>*/.class.getName());
    }
    
    /*<methods>*//*</methods>*/
    
    /*<annotations>*/@Library("HomeKit") @StronglyLinked/*</annotations>*/
    public static class Values {
    	static { Bro.bind(Values.class); }

        /*<values>*/
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypePowerState", optional=true)
        public static native NSString PowerState();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeHue", optional=true)
        public static native NSString Hue();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeSaturation", optional=true)
        public static native NSString Saturation();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeBrightness", optional=true)
        public static native NSString Brightness();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTemperatureUnits", optional=true)
        public static native NSString TemperatureUnits();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentTemperature", optional=true)
        public static native NSString CurrentTemperature();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetTemperature", optional=true)
        public static native NSString TargetTemperature();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentHeatingCooling", optional=true)
        public static native NSString CurrentHeatingCooling();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetHeatingCooling", optional=true)
        public static native NSString TargetHeatingCooling();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCoolingThreshold", optional=true)
        public static native NSString CoolingThreshold();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeHeatingThreshold", optional=true)
        public static native NSString HeatingThreshold();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentRelativeHumidity", optional=true)
        public static native NSString CurrentRelativeHumidity();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetRelativeHumidity", optional=true)
        public static native NSString TargetRelativeHumidity();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentDoorState", optional=true)
        public static native NSString CurrentDoorState();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetDoorState", optional=true)
        public static native NSString TargetDoorState();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeObstructionDetected", optional=true)
        public static native NSString ObstructionDetected();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeName", optional=true)
        public static native NSString Name();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeManufacturer", optional=true)
        public static native NSString Manufacturer();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeModel", optional=true)
        public static native NSString Model();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeSerialNumber", optional=true)
        public static native NSString SerialNumber();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeIdentify", optional=true)
        public static native NSString Identify();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeRotationDirection", optional=true)
        public static native NSString RotationDirection();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeRotationSpeed", optional=true)
        public static native NSString RotationSpeed();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeOutletInUse", optional=true)
        public static native NSString OutletInUse();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeVersion", optional=true)
        public static native NSString Version();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeLogs", optional=true)
        public static native NSString Logs();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeAudioFeedback", optional=true)
        public static native NSString AudioFeedback();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeAdminOnlyAccess", optional=true)
        public static native NSString AdminOnlyAccess();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeSecuritySystemAlarmType", optional=true)
        public static native NSString SecuritySystemAlarmType();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeMotionDetected", optional=true)
        public static native NSString MotionDetected();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentLockMechanismState", optional=true)
        public static native NSString CurrentLockMechanismState();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetLockMechanismState", optional=true)
        public static native NSString TargetLockMechanismState();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeLockMechanismLastKnownAction", optional=true)
        public static native NSString LockMechanismLastKnownAction();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeLockManagementControlPoint", optional=true)
        public static native NSString LockManagementControlPoint();
        /**
         * @since Available in iOS 8.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeLockManagementAutoSecureTimeout", optional=true)
        public static native NSString LockManagementAutoSecureTimeout();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeAirParticulateDensity", optional=true)
        public static native NSString AirParticulateDensity();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeAirParticulateSize", optional=true)
        public static native NSString AirParticulateSize();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeAirQuality", optional=true)
        public static native NSString AirQuality();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeBatteryLevel", optional=true)
        public static native NSString BatteryLevel();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCarbonDioxideDetected", optional=true)
        public static native NSString CarbonDioxideDetected();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCarbonDioxideLevel", optional=true)
        public static native NSString CarbonDioxideLevel();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCarbonDioxidePeakLevel", optional=true)
        public static native NSString CarbonDioxidePeakLevel();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCarbonMonoxideDetected", optional=true)
        public static native NSString CarbonMonoxideDetected();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCarbonMonoxideLevel", optional=true)
        public static native NSString CarbonMonoxideLevel();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCarbonMonoxidePeakLevel", optional=true)
        public static native NSString CarbonMonoxidePeakLevel();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeChargingState", optional=true)
        public static native NSString ChargingState();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeContactState", optional=true)
        public static native NSString ContactState();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentHorizontalTilt", optional=true)
        public static native NSString CurrentHorizontalTilt();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentLightLevel", optional=true)
        public static native NSString CurrentLightLevel();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentPosition", optional=true)
        public static native NSString CurrentPosition();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentSecuritySystemState", optional=true)
        public static native NSString CurrentSecuritySystemState();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeCurrentVerticalTilt", optional=true)
        public static native NSString CurrentVerticalTilt();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeFirmwareVersion", optional=true)
        public static native NSString FirmwareVersion();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeHardwareVersion", optional=true)
        public static native NSString HardwareVersion();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeHoldPosition", optional=true)
        public static native NSString HoldPosition();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeInputEvent", optional=true)
        public static native NSString InputEvent();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeLeakDetected", optional=true)
        public static native NSString LeakDetected();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeOccupancyDetected", optional=true)
        public static native NSString OccupancyDetected();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeOutputState", optional=true)
        public static native NSString OutputState();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypePositionState", optional=true)
        public static native NSString PositionState();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeSmokeDetected", optional=true)
        public static native NSString SmokeDetected();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeSoftwareVersion", optional=true)
        public static native NSString SoftwareVersion();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeStatusActive", optional=true)
        public static native NSString StatusActive();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeStatusFault", optional=true)
        public static native NSString StatusFault();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeStatusJammed", optional=true)
        public static native NSString StatusJammed();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeStatusLowBattery", optional=true)
        public static native NSString StatusLowBattery();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeStatusTampered", optional=true)
        public static native NSString StatusTampered();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetHorizontalTilt", optional=true)
        public static native NSString TargetHorizontalTilt();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetSecuritySystemState", optional=true)
        public static native NSString TargetSecuritySystemState();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetPosition", optional=true)
        public static native NSString TargetPosition();
        /**
         * @since Available in iOS 9.0 and later.
         */
        @GlobalValue(symbol="HMCharacteristicTypeTargetVerticalTilt", optional=true)
        public static native NSString TargetVerticalTilt();
        /*</values>*/
    }
}

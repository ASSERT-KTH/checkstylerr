package io.openems.common.channel;

import com.google.common.base.CaseFormat;

import io.openems.common.types.OpenemsType;

public enum Unit {
	// ##########
	// Generic
	// ##########

	/**
	 * No Unit
	 */
	NONE(""),

	/**
	 * Percentage [%], 0-100
	 */
	PERCENT("%"),

	/**
	 * Thousandth [‰], 0-1000
	 */
	THOUSANDTH("‰"),

	/**
	 * On or Off
	 */
	ON_OFF(""),

	// ##########
	// Power
	// ##########

	/**
	 * Unit of Active Power [W]
	 */
	WATT("W"),

	/**
	 * Unit of Active Power [mW]
	 */
	MILLIWATT("mW", WATT, -3),

	/**
	 * Unit of Active Power [kW]
	 */
	KILOWATT("kW", WATT, 3),

	/**
	 * Unit of Reactive Power [var]
	 */
	VOLT_AMPERE_REACTIVE("var"),

	/**
	 * Unit of Reactive Power [kvar]
	 */
	KILOVOLT_AMPERE_REACTIVE("kvar", VOLT_AMPERE_REACTIVE, 3),

	/**
	 * Unit of Apparent Power [VA]
	 */
	VOLT_AMPERE("VA"),

	/**
	 * Unit of Apparent Power [kVA]
	 */
	KILOVOLT_AMPERE("kVA", VOLT_AMPERE, 3),

	// ##########
	// Voltage
	// ##########

	/**
	 * Unit of Voltage [V]
	 */
	VOLT("V"),

	/**
	 * Unit of Voltage [mV]
	 */
	MILLIVOLT("mV", VOLT, -3),

	// ##########
	// Current
	// ##########

	/**
	 * Unit of Current [A]
	 */
	AMPERE("A"),

	/**
	 * Unit of Current [mA]
	 */
	MILLIAMPERE("mA", AMPERE, -3),

	// ##########
	// Electric Charge
	// ##########

	/**
	 * Unit of Electric Charge
	 */
	AMPERE_HOURS("Ah"),

	/**
	 * Unit of Electric Charge
	 */
	MILLIAMPERE_HOURS("mAh", AMPERE_HOURS, -3),

	/**
	 * Unit of Electric Charge
	 */
	KILOAMPERE_HOURS("kAh", AMPERE_HOURS, 3),

	// ##########
	// Energy
	// ##########

	/**
	 * Unit of Energy [Wh]
	 */
	WATT_HOURS("Wh"),

	/**
	 * Unit of Energy [kWh]
	 */
	KILOWATT_HOURS("kWh", WATT_HOURS, 3),

	/**
	 * Unit of Reactive Energy [varh]
	 */
	VOLT_AMPERE_REACTIVE_HOURS("varh"),

	/**
	 * Unit of Reactive Energy [kVArh]
	 */
	KILOVOLT_AMPERE_REACTIVE_HOURS("kvarh", VOLT_AMPERE_REACTIVE_HOURS, 3),

	/**
	 * Unit of Energy [Wh/Wp]
	 */
	WATT_HOURS_BY_WATT_PEAK("Wh/Wp"),

	/**
	 * Unit of Apparent Energy [VAh]
	 */
	VOLT_AMPERE_HOURS("VAh"),

	// ##########
	// Frequency
	// ##########

	/**
	 * Unit of Frequency [Hz]
	 */
	HERTZ("Hz"),

	/**
	 * Unit of Frequency [mHz]
	 */
	MILLIHERTZ("mHz", HERTZ, -3),

	// ##########
	// Temperature
	// ##########

	/**
	 * Unit of Temperature [C]
	 */
	DEGREE_CELSIUS("C"),

	/**
	 * Unit of Temperature [dC]
	 */
	DEZIDEGREE_CELSIUS("dC", DEGREE_CELSIUS, -1),

	// ##########
	// Time
	// ##########

	/**
	 * Unit of Time [s]
	 */
	SECONDS("sec"),

	/**
	 * Unit of Time [ms]
	 */
	MILLISECONDS("ms", SECONDS, -3),

	/**
	 * Unit of Time
	 */
	MINUTE("min"),

	/**
	 * Unit of Time
	 */
	HOUR("h"),

	// ##########
	// Cumulated Time
	// ##########

	/**
	 * Unit of cumulated time [s]
	 */
	CUMULATED_SECONDS("sec"),

	// ##########
	// Resistance
	// ##########

	/**
	 * Unit of Resistance [Ohm]
	 */
	OHM("Ohm"),

	/**
	 * Unit of Resistance [kOhm]
	 */
	KILOOHM("kOhm", OHM, 3),

	/**
	 * Unit of Resistance [mOhm]
	 */
	MILLIOHM("mOhm", OHM, -3),

	/**
	 * Unit of Resistance [uOhm]
	 */
	MICROOHM("uOhm", OHM, -6);

	private final Unit baseUnit;
	private final int scaleFactor;
	private final String symbol;

	private Unit(String symbol) {
		this(symbol, null, 0);
	}

	private Unit(String symbol, Unit baseUnit, int scaleFactor) {
		this.symbol = symbol;
		this.baseUnit = baseUnit;
		this.scaleFactor = scaleFactor;
	}

	public Unit getBaseUnit() {
		return baseUnit;
	}

	public int getAsBaseUnit(int value) {
		return (int) (value * Math.pow(10, this.scaleFactor));
	}

	public String getSymbol() {
		return symbol;
	}

	public String format(Object value, OpenemsType type) {
		switch (this) {
		case NONE:
			return value.toString();
		case AMPERE:
		case DEGREE_CELSIUS:
		case DEZIDEGREE_CELSIUS:
		case HERTZ:
		case MILLIAMPERE:
		case MILLIHERTZ:
		case MILLIVOLT:
		case PERCENT:
		case VOLT:
		case VOLT_AMPERE:
		case VOLT_AMPERE_REACTIVE:
		case WATT:
		case KILOWATT:
		case MILLIWATT:
		case WATT_HOURS:
		case OHM:
		case KILOOHM:
		case SECONDS:
		case AMPERE_HOURS:
		case HOUR:
		case CUMULATED_SECONDS:
		case KILOAMPERE_HOURS:
		case KILOVOLT_AMPERE:
		case KILOVOLT_AMPERE_REACTIVE:
		case KILOVOLT_AMPERE_REACTIVE_HOURS:
		case KILOWATT_HOURS:
		case MICROOHM:
		case MILLIAMPERE_HOURS:
		case MILLIOHM:
		case MILLISECONDS:
		case MINUTE:
		case THOUSANDTH:
		case VOLT_AMPERE_HOURS:
		case VOLT_AMPERE_REACTIVE_HOURS:
		case WATT_HOURS_BY_WATT_PEAK:
			return value + " " + this.symbol;
		case ON_OFF:
			boolean booleanValue = (Boolean) value;
			return booleanValue ? "ON" : "OFF";
		}
		return "FORMAT_ERROR"; // should never happen, if 'switch' is complete
	}

	public String formatAsBaseUnit(Object value, OpenemsType type) {
		if (this.baseUnit != null) {
			switch (type) {
			case SHORT:
			case INTEGER:
			case LONG:
			case FLOAT:
			case DOUBLE:
				return this.baseUnit.formatAsBaseUnit(this.getAsBaseUnit((int) value), type);
			case BOOLEAN:
			case STRING:
				return this.baseUnit.formatAsBaseUnit(value, type);
			}
		} else {
			this.format(value, type);
		}
		return "FORMAT_ERROR"; // should never happen, if 'switch' is complete
	}

	@Override
	public String toString() {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name()) + //
				(this.symbol.isEmpty() ? "" : " [" + this.symbol + "]");
	}
}

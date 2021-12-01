package io.openems.edge.sma.enums;

import io.openems.common.types.OptionsEnum;

public enum AutomaticFrequencySynchronization implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	OFF(303, "Off"), //
	ON(308, "On");

	private final int value;
	private final String name;

	private AutomaticFrequencySynchronization(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}
}
package io.openems.edge.sma.enums;

import io.openems.common.types.OptionsEnum;

public enum ConfigurationOfTheCosphiEndPoint implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	LEADING(1041, "Leading"), //
	LAGGING(1042, "Lagging");

	private final int value;
	private final String name;

	private ConfigurationOfTheCosphiEndPoint(int value, String name) {
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
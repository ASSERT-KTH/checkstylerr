package io.openems.edge.controller.evcs;

import io.openems.common.types.OptionsEnum;

public enum ChargeMode implements OptionsEnum {
	FORCE_CHARGE(0, "Force-Charge"), //
	EXCESS_POWER(1, "Use excessive power"); //

	private final int value;
	private final String name;

	private ChargeMode(int value, String name) {
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
		return FORCE_CHARGE;
	}
}
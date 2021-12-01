package io.openems.edge.common.modbusslave;

import java.nio.charset.StandardCharsets;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.type.TypeUtils;

public class ModbusRecordString16 extends ModbusRecordConstant {

	public final static byte[] UNDEFINED_VALUE = new byte[32];

	public final static int BYTE_LENGTH = 32;

	private final String value;

	public ModbusRecordString16(int offset, String name, String value) {
		super(offset, name, ModbusType.STRING16, toByteArray(value));
		this.value = value;
	}

	@Override
	public String toString() {
		return "ModbusRecordString16 [value=" + this.value + ", type=" + getType() + "]";
	}

	public static byte[] toByteArray(String value) {
		byte[] result = new byte[BYTE_LENGTH];
		byte[] converted = value.getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(converted, 0, result, 0, Math.min(BYTE_LENGTH, converted.length));
		return result;
	}

	public static byte[] toByteArray(Object value) {
		if (value == null) {
			return UNDEFINED_VALUE;
		} else {
			return toByteArray((String) TypeUtils.getAsType(OpenemsType.STRING, value));
		}
	}

	@Override
	public String getValueDescription() {
		return this.value != null ? this.value : "";
	}

}

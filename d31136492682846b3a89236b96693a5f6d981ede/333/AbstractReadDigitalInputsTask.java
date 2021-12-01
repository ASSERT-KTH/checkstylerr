package io.openems.edge.bridge.modbus.api.task;

import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.util.BitVector;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.element.AbstractModbusElement;
import io.openems.edge.bridge.modbus.api.element.ModbusCoilElement;
import io.openems.edge.bridge.modbus.api.element.ModbusElement;
import io.openems.edge.common.taskmanager.Priority;

public abstract class AbstractReadDigitalInputsTask extends AbstractReadTask<Boolean> {

	public AbstractReadDigitalInputsTask(int startAddress, Priority priority, AbstractModbusElement<?>... elements) {
		super(startAddress, priority, elements);
	}

	@Override
	protected boolean isCorrectElementInstance(ModbusElement<?> modbusElement) {
		return modbusElement instanceof ModbusCoilElement;
	}

	@Override
	protected String getRequiredElementName() {
		return "ModbusCoilElement";
	}

	@Override
	protected void doElementSetInput(ModbusElement<?> modbusElement, int position, Boolean[] response)
			throws OpenemsException {
		((ModbusCoilElement) modbusElement).setInputCoil((Boolean) response[position]);
	}

	@Override
	protected int increasePosition(int position, ModbusElement<?> modbusElement) {
		return position + 1;
	}

	@Override
	protected Boolean[] handleResponse(ModbusResponse response) throws OpenemsException {
		try {
			return (Utils.toBooleanArray(getBitVector(response)));
		} catch (ClassCastException e) {
			throw new OpenemsException("Unexpected Modbus response. Expected [" + getExpectedInputClassname()
					+ "], got [" + response.getClass().getSimpleName() + "]");
		}
	}

	protected abstract String getExpectedInputClassname();

	protected abstract BitVector getBitVector(ModbusResponse response) throws OpenemsException;
}
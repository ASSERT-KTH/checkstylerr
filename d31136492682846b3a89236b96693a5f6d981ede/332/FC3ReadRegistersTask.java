package io.openems.edge.bridge.modbus.api.task;

import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.element.AbstractModbusElement;
import io.openems.edge.common.taskmanager.Priority;

/**
 * Implements a Read Holding Register abstractTask, implementing Modbus function
 * code 3 (http://www.simplymodbus.ca/FC03.htm)
 */
public class FC3ReadRegistersTask extends AbstractReadInputRegistersTask implements ReadTask {

	public FC3ReadRegistersTask(int startAddress, Priority priority, AbstractModbusElement<?>... elements) {
		super(startAddress, priority, elements);
	}

	@Override
	protected ModbusRequest getRequest() {
		return new ReadMultipleRegistersRequest(getStartAddress(), getLength());
	}

	@Override
	protected InputRegister[] handleResponse(ModbusResponse response) throws OpenemsException {
		if (response instanceof ReadMultipleRegistersResponse) {
			ReadMultipleRegistersResponse registersResponse = (ReadMultipleRegistersResponse) response;
			return registersResponse.getRegisters();
		} else {
			throw new OpenemsException("Unexpected Modbus response. Expected [ReadMultipleRegistersResponse], got ["
					+ response.getClass().getSimpleName() + "]");
		}
	}

	@Override
	protected String getActiondescription() {
		return "FC3ReadHoldingRegisters";
	}
}

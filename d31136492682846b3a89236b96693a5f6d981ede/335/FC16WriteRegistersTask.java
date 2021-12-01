package io.openems.edge.bridge.modbus.api.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.procimg.Register;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.AbstractModbusBridge;
import io.openems.edge.bridge.modbus.api.element.AbstractModbusElement;
import io.openems.edge.bridge.modbus.api.element.ModbusElement;
import io.openems.edge.bridge.modbus.api.element.ModbusRegisterElement;

/**
 * Implements a Write Holding Registers abstractTask, using Modbus function code
 * 16 (http://www.simplymodbus.ca/FC16.htm)
 */
public class FC16WriteRegistersTask extends AbstractTask implements WriteTask {

	private final Logger log = LoggerFactory.getLogger(FC16WriteRegistersTask.class);

	public FC16WriteRegistersTask(int startAddress, AbstractModbusElement<?>... elements) {
		super(startAddress, elements);
	}

	private class CombinedWriteRegisters {
		public final int startAddress;
		private final List<Register> registers = new ArrayList<>();

		public CombinedWriteRegisters(int startAddress) {
			this.startAddress = startAddress;
		}

		public void add(Register... registers) {
			for (Register register : registers) {
				this.registers.add(register);
			}
		}

		public Register[] getRegisters() {
			return this.registers.toArray(new Register[this.registers.size()]);
		}

		public int getLastAddress() {
			return this.startAddress + registers.size() - 1;
		}
	}

	@Override
	public int _execute(AbstractModbusBridge bridge) throws OpenemsException {
		int noOfWrittenRegisters = 0;
		List<CombinedWriteRegisters> writes = mergeWriteRegisters();
		// Execute combined writes
		for (CombinedWriteRegisters write : writes) {
			Register[] registers = write.getRegisters();
			try {
				/*
				 * First try
				 */
				this.writeMultipleRegisters(bridge, this.getParent().getUnitId(), write.startAddress, registers);
			} catch (OpenemsException | ModbusException e) {
				/*
				 * Second try: with new connection
				 */
				bridge.closeModbusConnection();
				try {
					this.writeMultipleRegisters(bridge, this.getParent().getUnitId(), write.startAddress,
							write.getRegisters());
				} catch (ModbusException e2) {
					throw new OpenemsException("Transaction failed: " + e.getMessage(), e2);
				}
			}
			noOfWrittenRegisters += registers.length;
		}
		return noOfWrittenRegisters;
	}

	private void writeMultipleRegisters(AbstractModbusBridge bridge, int unitId, int startAddress, Register[] registers)
			throws ModbusException, OpenemsException {
		WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(startAddress, registers);
		ModbusResponse response = Utils.getResponse(request, unitId, bridge);

		// debug output
		switch (this.getLogVerbosity(bridge)) {
		case READS_AND_WRITES:
			bridge.logInfo(this.log, "FC16WriteRegisters " //
					+ "[" + unitId + ":" + startAddress + "/0x" + Integer.toHexString(startAddress) + "]: " //
					+ Arrays.stream(registers) //
							.map(r -> String.format("%4s", Integer.toHexString(r.getValue())).replace(' ', '0')) //
							.collect(Collectors.joining(" ")));
			break;
		case WRITES:
		case NONE:
			break;
		}

		if (!(response instanceof WriteMultipleRegistersResponse)) {
			throw new OpenemsException("Unexpected Modbus response. Expected [WriteMultipleRegistersResponse], got ["
					+ response.getClass().getSimpleName() + "]");
		}
	}

	/**
	 * Combine WriteRegisters without holes in between.
	 * 
	 * @return a list of CombinedWriteRegisters
	 */
	private List<CombinedWriteRegisters> mergeWriteRegisters() {
		List<CombinedWriteRegisters> writes = new ArrayList<>();
		ModbusElement<?>[] elements = this.getElements();
		for (int i = 0; i < elements.length; i++) {
			ModbusElement<?> element = elements[i];
			if (element instanceof ModbusRegisterElement) {
				Optional<Register[]> valueOpt = ((ModbusRegisterElement<?>) element).getNextWriteValueAndReset();
				if (valueOpt.isPresent()) {
					// found value -> add to 'writes'
					CombinedWriteRegisters write;
					if (writes.isEmpty() /* no writes created yet */
							|| writes.get(writes.size() - 1).getLastAddress() + 1 != element
									.getStartAddress() /* there is a hole between last element and current element */) {
						write = new CombinedWriteRegisters(element.getStartAddress());
						writes.add(write);
					} else {
						write = writes.get(writes.size() - 1); // no hole -> combine writes
					}
					write.add(valueOpt.get());
				}
			} else {
				log.warn("Unable to execute Write for ModbusElement [" + element + "]: No ModbusRegisterElement!");
			}
		}
		return writes;
	}

	@Override
	protected String getActiondescription() {
		return "FC16 Write Registers";
	}
}

package io.openems.edge.bridge.modbus.api.task;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.AbstractModbusBridge;
import io.openems.edge.bridge.modbus.api.element.AbstractModbusElement;
import io.openems.edge.bridge.modbus.api.element.ModbusElement;
import io.openems.edge.common.taskmanager.Priority;

/**
 * An abstract Modbus 'AbstractTask' is holding references to one or more Modbus
 * {@link AbstractModbusElement} which have register addresses in the same
 * range.
 */
public abstract class AbstractReadTask<T> extends AbstractTask implements ReadTask {

	private final Logger log = LoggerFactory.getLogger(AbstractReadTask.class);

	private final Priority priority;

	public AbstractReadTask(int startAddress, Priority priority, AbstractModbusElement<?>... elements) {
		super(startAddress, elements);
		this.priority = priority;
	}

	public int _execute(AbstractModbusBridge bridge) throws OpenemsException {
		T[] response;
		try {
			/*
			 * First try
			 */
			response = this.readElements(bridge);

		} catch (OpenemsException | ModbusException e) {
			/*
			 * Second try: with new connection
			 */
			bridge.closeModbusConnection();
			try {
				response = this.readElements(bridge);

			} catch (ModbusException e2) {
				for (ModbusElement<?> elem : this.getElements()) {
					if (!elem.isIgnored()) {
						elem.invalidate(bridge);
					}
				}
				throw new OpenemsException("Transaction failed: " + e.getMessage(), e2);
			}
		}

		// Verify response length
		if (response.length < getLength()) {
			throw new OpenemsException(
					"Received message is too short. Expected [" + getLength() + "], got [" + response.length + "]");
		}

		this.fillElements(response);
		return 1;
	}

	protected T[] readElements(AbstractModbusBridge bridge) throws OpenemsException, ModbusException {
		ModbusRequest request = this.getRequest();
		int unitId = this.getParent().getUnitId();
		ModbusResponse response = Utils.getResponse(request, this.getParent().getUnitId(), bridge);

		T[] result = this.handleResponse(response);

		// debug output
		switch (this.getLogVerbosity(bridge)) {
		case READS_AND_WRITES:
			bridge.logInfo(this.log, this.getActiondescription() //
					+ " [" + unitId + ":" + this.getStartAddress() + "/0x" + Integer.toHexString(this.getStartAddress())
					+ "]: " //
					+ Arrays.stream(result).map(r -> {
						if (r instanceof InputRegister) {
							return String.format("%4s", Integer.toHexString(((InputRegister) r).getValue()))
									.replace(' ', '0');
						} else if (r instanceof Boolean) {
							return ((Boolean) r) ? "x" : "-";
						} else {
							return r.toString();
						}
					}) //
							.collect(Collectors.joining(" ")));
			break;
		case WRITES:
		case NONE:
			break;
		}

		return result;
	}

	protected void fillElements(T[] response) {
		int position = 0;
		for (ModbusElement<?> modbusElement : this.getElements()) {
			if (!(this.isCorrectElementInstance(modbusElement))) {
				this.doErrorLog(modbusElement);
			} else {
				try {
					if (!modbusElement.isIgnored()) {
						this.doElementSetInput(modbusElement, position, response);
					}
				} catch (OpenemsException e) {
					this.doWarnLog(e);
				}
			}
			position = this.increasePosition(position, modbusElement);
		}
	}

	public Priority getPriority() {
		return priority;
	}

	protected abstract int increasePosition(int position, ModbusElement<?> modbusElement);

	protected abstract void doElementSetInput(ModbusElement<?> modbusElement, int position, T[] response)
			throws OpenemsException;

	protected abstract String getRequiredElementName();

	protected abstract boolean isCorrectElementInstance(ModbusElement<?> modbusElement);

	protected abstract ModbusRequest getRequest();

	protected abstract T[] handleResponse(ModbusResponse response) throws OpenemsException;

	private void doWarnLog(OpenemsException e) {
		log.warn("Unable to fill modbus element. UnitId [" + this.getParent().getUnitId() + "] Address ["
				+ getStartAddress() + "] Length [" + getLength() + "]: " + e.getMessage());
	}

	private void doErrorLog(ModbusElement<?> modbusElement) {
		log.error("A " + getRequiredElementName() + " is required for a " + getActiondescription() + "Task! Element ["
				+ modbusElement + "]");
	}

}

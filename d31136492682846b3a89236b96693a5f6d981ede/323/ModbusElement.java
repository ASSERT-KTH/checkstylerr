package io.openems.edge.bridge.modbus.api.element;

import java.util.Optional;
import java.util.function.Consumer;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractModbusBridge;
import io.openems.edge.bridge.modbus.api.task.AbstractTask;

/**
 * A ModbusElement represents one or more registers or coils in an
 * {@link AbstractTask}.
 */
public interface ModbusElement<T> {

	/**
	 * Gets the start address of this Modbus element.
	 * 
	 * @return the start address
	 */
	public int getStartAddress();

	/**
	 * Number of Registers or Coils.
	 * 
	 * @return the number of Registers or Coils
	 */
	public abstract int getLength();

	/**
	 * Set the {@link AbstractTask}, where this Element belongs to.
	 * 
	 * <p>
	 * This is called by the {@link AbstractTask} constructor.
	 *
	 * @param abstractTask the AbstractTask
	 */
	public void setModbusTask(AbstractTask abstractTask);

	/**
	 * Whether this Element should be ignored (= DummyElement).
	 * 
	 * @return true for ignored elements
	 */
	public boolean isIgnored();

	/**
	 * Gets the type of this Register, e.g. INTEGER, BOOLEAN,..
	 * 
	 * @return the OpenemsType
	 */
	public OpenemsType getType();

	/**
	 * Sets a value that should be written to the Modbus device.
	 * 
	 * @param valueOpt the Optional value
	 * @throws OpenemsException         on error
	 * @throws IllegalArgumentException on error
	 */
	public void _setNextWriteValue(Optional<T> valueOpt) throws OpenemsException, IllegalArgumentException;

	/**
	 * Add an onSetNextWrite callback. It is called when a 'next write value' was
	 * set.
	 * 
	 * @param callback the callback
	 */
	public void onSetNextWrite(Consumer<Optional<T>> callback);

	/**
	 * Invalidates the Channel in case it could not be read from the Modbus device,
	 * i.e. sets the value to 'UNDEFINED'/null. Applies the
	 * 'invalidateElementsAfterReadErrors' config setting of the bridge.
	 * 
	 * @param the {@link AbstractModbusBridge}
	 * @return true if Channel was invalidated
	 */
	public boolean invalidate(AbstractModbusBridge bridge);

	/**
	 * This is called on deactivate of the Modbus-Bridge. It can be used to clear
	 * any references like listeners.
	 */
	public void deactivate();
}

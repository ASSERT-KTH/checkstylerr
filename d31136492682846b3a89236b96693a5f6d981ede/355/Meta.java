package io.openems.edge.common.meta;

import io.openems.common.OpenemsConstants;
import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;

public interface Meta extends ModbusSlave {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		/**
		 * OpenEMS Version
		 * 
		 * <ul>
		 * <li>Interface: Meta
		 * <li>Type: String
		 * </ul>
		 */
		VERSION(Doc.of(OpenemsType.STRING));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		public Doc doc() {
			return this.doc;
		}
	}

	@Override
	public default ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable( //
				ModbusSlaveNatureTable.of(Meta.class, accessMode, 199) //
						.uint16(0, "OpenEMS Version Major", OpenemsConstants.VERSION_MAJOR) //
						.uint16(1, "OpenEMS Version Minor", OpenemsConstants.VERSION_MINOR) //
						.uint16(2, "OpenEMS Version Patch", OpenemsConstants.VERSION_PATCH) //
						.string16(3, "Manufacturer", OpenemsConstants.MANUFACTURER) //
						.string16(19, "Manufacturer Model", OpenemsConstants.MANUFACTURER_MODEL) //
						.string16(35, "Manufacturer Options", OpenemsConstants.MANUFACTURER_OPTIONS) //
						.string16(51, "Manufacturer Version", OpenemsConstants.MANUFACTURER_VERSION) //
						.string16(67, "Manufacturer Serial Number", OpenemsConstants.MANUFACTURER_SERIAL_NUMBER) //
						.string16(83, "Manufacturer EMS Serial Number", OpenemsConstants.MANUFACTURER_EMS_SERIAL_NUMBER) //
						.build());
	}

}

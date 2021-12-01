package io.openems.edge.ess.sinexcel;

import java.time.LocalDateTime;
import java.util.Optional;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverterChain;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.BitsWordElement;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.StringWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.channel.BooleanReadChannel;
import io.openems.edge.common.channel.EnumWriteChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.startstop.StartStop;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.Constraint;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Power;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.ess.power.api.Relationship;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Ess.Sinexcel", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE }) //
public class EssSinexcelImpl extends AbstractOpenemsModbusComponent
		implements EssSinexcel, SymmetricEss, ManagedSymmetricEss, EventHandler, OpenemsComponent, ModbusSlave {

	private final Logger log = LoggerFactory.getLogger(EssSinexcelImpl.class);

	public static final int MAX_APPARENT_POWER = 30_000;
	public static final int DEFAULT_UNIT_ID = 1;

	public int maxApparentPower;
	private InverterState inverterState;
	private Battery battery;
	public LocalDateTime timeForSystemInitialization = null;

	protected int slowChargeVoltage = 4370; // for new batteries - 3940
	protected int floatChargeVoltage = 4370; // for new batteries - 3940

	private int counterOff = 0;

	// State-Machines
	private final StateMachine stateMachine;

	/**
	 * Helper wrapping class to handle listeners on battery Channels.
	 */
	private final ChannelManager channelHandler = new ChannelManager(this);

	@Reference
	protected ComponentManager componentManager;

	protected Config config;

	@Reference
	protected ConfigurationAdmin cm;

	@Reference
	private Power power;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setBattery(Battery battery) {
		this.battery = battery;
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		if (super.activate(context, config.id(), config.alias(), config.enabled(), DEFAULT_UNIT_ID, this.cm, "Modbus",
				config.modbus_id())) {
			return;
		}
		this.config = config;
		this.inverterState = config.InverterState();

		// initialize the connection to the battery
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "Battery", config.battery_id())) {
			return;
		}
		this.channelHandler.activate(this.componentManager, this.battery);

		this.slowChargeVoltage = config.toppingCharge();
		this.floatChargeVoltage = config.toppingCharge();

		// this.getNoOfCells();
		this.inverterOn();
	}

	@Deactivate
	protected void deactivate() {
		this.channelHandler.deactivate();
		super.deactivate();
	}

	public EssSinexcelImpl() throws OpenemsNamedException {
		super(//
				OpenemsComponent.ChannelId.values(), //
				SymmetricEss.ChannelId.values(), //
				ManagedSymmetricEss.ChannelId.values(), //
				EssSinexcel.ChannelId.values() //
		);
		this._setMaxApparentPower(EssSinexcelImpl.MAX_APPARENT_POWER);
		this.stateMachine = new StateMachine(this);
	}

//	private void getNoOfCells() throws OpenemsNamedException {
//		Battery bms = this.componentManager.getComponent(this.config.battery_id());
//		this.numberOfSlaves = (int) bms.getComponentContext().getProperties().get("numberOfSlaves");
//	}

	private final static int MAX_CURRENT = 90; // [A]

	/**
	 * Sets the Battery Ranges. Executed on TOPIC_CYCLE_AFTER_PROCESS_IMAGE.
	 * 
	 * @throws OpenemsNamedException
	 */
	private void setBatteryRanges() throws OpenemsNamedException {
		final int disMaxA;
		final int chaMaxA;
		final int disMinV;
		final int chaMaxV;

		// Evaluate input data
		if (battery == null) {
			disMaxA = 0;
			chaMaxA = 0;
			disMinV = 0;
			chaMaxV = 0;
		} else {
			disMaxA = battery.getDischargeMaxCurrent().orElse(0);
			chaMaxA = battery.getChargeMaxCurrent().orElse(0);
			disMinV = battery.getDischargeMinVoltage().orElse(0);
			chaMaxV = battery.getChargeMaxVoltage().orElse(0);
		}

		// Set Inverter Registers
		{
			IntegerWriteChannel chargeMaxCurrentChannel = this.channel(EssSinexcel.ChannelId.CHARGE_MAX_A);
			chargeMaxCurrentChannel.setNextWriteValue(//
					/* enforce positive */ Math.max(0, //
							/* apply max current */ Math.min(MAX_CURRENT, chaMaxA) //
					) * 10);
		}
		{
			IntegerWriteChannel dischargeMaxCurrentChannel = this.channel(EssSinexcel.ChannelId.DISCHARGE_MAX_A);
			dischargeMaxCurrentChannel.setNextWriteValue(//
					/* enforce positive */ Math.max(0, //
							/* apply max current */ Math.min(MAX_CURRENT, disMaxA) //
					) * 10);
		}
		{
			IntegerWriteChannel dischargeMinVoltageChannel = this.channel(EssSinexcel.ChannelId.DISCHARGE_MIN_V);
			dischargeMinVoltageChannel.setNextWriteValue(disMinV * 10);
		}
		{
			IntegerWriteChannel chargeMaxVoltageChannel = this.channel(EssSinexcel.ChannelId.CHARGE_MAX_V);
			chargeMaxVoltageChannel.setNextWriteValue(chaMaxV * 10);
		}
	}

	/**
	 * Starts the inverter.
	 * 
	 * @throws OpenemsNamedException on error
	 */
	public void inverterOn() throws OpenemsNamedException {
		EnumWriteChannel setdataModOnCmd = this.channel(EssSinexcel.ChannelId.MOD_ON_CMD);
		setdataModOnCmd.setNextWriteValue(FalseTrue.TRUE); // true = START
	}

	/**
	 * Stops the inverter.
	 * 
	 * @throws OpenemsNamedException on error
	 */
	public void inverterOff() throws OpenemsNamedException {
		EnumWriteChannel setdataModOffCmd = this.channel(EssSinexcel.ChannelId.MOD_OFF_CMD);
		setdataModOffCmd.setNextWriteValue(FalseTrue.TRUE); // true = STOP
	}

	/**
	 * Executes a Soft-Start. Sets the internal DC relay. Once this register is set
	 * to 1, the PCS will start the soft-start procedure, otherwise, the PCS will do
	 * nothing on the DC input Every time the PCS is powered off, this register will
	 * be cleared to 0. In some particular cases, the BMS wants to re-softstart, the
	 * EMS should actively clear this register to 0, after BMS soft-started, set it
	 * to 1 again.
	 *
	 * @throws OpenemsNamedException on error
	 */
	public void softStart(boolean switchOn) throws OpenemsNamedException {
		this.logInfo(this.log, "[In boolean soft start method]");
		IntegerWriteChannel setDcRelay = this.channel(EssSinexcel.ChannelId.SET_INTERN_DC_RELAY);
		if (switchOn) {
			setDcRelay.setNextWriteValue(1);
		} else {
			setDcRelay.setNextWriteValue(0);
		}
	}

	/**
	 * At first the PCS needs a stop command, then is required to remove the AC
	 * connection, after that the Grid OFF command.
	 * 
	 * @throws OpenemsNamedException on error
	 */
	public void islandOn() throws OpenemsNamedException {
		IntegerWriteChannel setAntiIslanding = this.channel(EssSinexcel.ChannelId.SET_ANTI_ISLANDING);
		setAntiIslanding.setNextWriteValue(0); // Disabled
		IntegerWriteChannel setdataGridOffCmd = this.channel(EssSinexcel.ChannelId.OFF_GRID_CMD);
		setdataGridOffCmd.setNextWriteValue(1); // Stop
	}

	/**
	 * At first the PCS needs a stop command, then is required to plug in the AC
	 * connection, after that the Grid ON command.
	 * 
	 * @throws OpenemsNamedException on error
	 */
	public void islandingOff() throws OpenemsNamedException {
		IntegerWriteChannel setAntiIslanding = this.channel(EssSinexcel.ChannelId.SET_ANTI_ISLANDING);
		setAntiIslanding.setNextWriteValue(1); // Enabled
		IntegerWriteChannel setdataGridOnCmd = this.channel(EssSinexcel.ChannelId.OFF_GRID_CMD);
		setdataGridOnCmd.setNextWriteValue(1); // Start
	}

	public void doHandlingSlowFloatVoltage() throws OpenemsNamedException {
		// System.out.println("Upper voltage : " +
		// this.channel(EssSinexcel.ChannelId.UPPER_VOLTAGE_LIMIT).value().asStringWithoutUnit());
		IntegerWriteChannel setSlowChargeVoltage = this.channel(EssSinexcel.ChannelId.SET_SLOW_CHARGE_VOLTAGE);
		setSlowChargeVoltage.setNextWriteValue(this.slowChargeVoltage);
		IntegerWriteChannel setFloatChargeVoltage = this.channel(EssSinexcel.ChannelId.SET_FLOAT_CHARGE_VOLTAGE);
		setFloatChargeVoltage.setNextWriteValue(this.floatChargeVoltage);
	}

	public boolean faultIslanding() {
		StateChannel i = this.channel(EssSinexcel.ChannelId.STATE_4);
		Optional<Boolean> islanding = i.getNextValue().asOptional();
		return islanding.isPresent() && islanding.get();
	}

	public boolean stateOnOff() {
		BooleanReadChannel v = this.channel(EssSinexcel.ChannelId.STATE_18);
		Optional<Boolean> stateOff = v.getNextValue().asOptional();
		return stateOff.isPresent() && stateOff.get();
	}

//	/**
//	 * Is Grid Shutdown?.
//	 * 
//	 * @return true if grid is shut down
//	 */
//	public boolean faultIslanding() {
//		StateChannel channel = this.channel(EssSinexcel.ChannelId.STATE_4);
//		Optional<Boolean> islanding = channel.getNextValue().asOptional();
//		return islanding.isPresent() && islanding.get();
//	}
//
//	/**
//	 * Is inverter state ON?.
//	 * 
//	 * @return true if inverter is in ON-State
//	 */
//	public boolean isStateOn() {
//		StateChannel channel = this.channel(EssSinexcel.ChannelId.STATE_18);
//		Optional<Boolean> stateOff = channel.getNextValue().asOptional();
//		return stateOff.isPresent() && stateOff.get();
//	}

	// SF: was commented before
//	public boolean stateOn() {
//		StateChannel v = this.channel(EssSinexcel.ChannelId.Sinexcel_STATE_9);
//		Optional<Boolean> stateOff = v.getNextValue().asOptional(); 
//		return stateOff.isPresent() && stateOff.get();
//	}

	protected ModbusProtocol defineModbusProtocol() throws OpenemsException {
		return new ModbusProtocol(this, //

				new FC6WriteRegisterTask(0x028A, //
						m(EssSinexcel.ChannelId.MOD_ON_CMD, new UnsignedWordElement(0x028A))),
				new FC6WriteRegisterTask(0x028B, //
						m(EssSinexcel.ChannelId.MOD_OFF_CMD, new UnsignedWordElement(0x028B))),
				new FC6WriteRegisterTask(0x028C, //
						m(EssSinexcel.ChannelId.CLEAR_FAILURE_CMD, new UnsignedWordElement(0x028C))),
				new FC6WriteRegisterTask(0x028D, //
						m(EssSinexcel.ChannelId.ON_GRID_CMD, new UnsignedWordElement(0x028D))),
				new FC6WriteRegisterTask(0x028E, //
						m(EssSinexcel.ChannelId.OFF_GRID_CMD, new UnsignedWordElement(0x028E))),

				new FC6WriteRegisterTask(0x0290, // FIXME: not documented!
						m(EssSinexcel.ChannelId.SET_INTERN_DC_RELAY, new UnsignedWordElement(0x0290))),

				new FC6WriteRegisterTask(0x0087, //
						m(EssSinexcel.ChannelId.SET_ACTIVE_POWER, new SignedWordElement(0x0087))), // in 100 W
				new FC6WriteRegisterTask(0x0088,
						m(EssSinexcel.ChannelId.SET_REACTIVE_POWER, new SignedWordElement(0x0088))), // in 100 var

				new FC6WriteRegisterTask(0x032B, //
						m(EssSinexcel.ChannelId.CHARGE_MAX_A, new UnsignedWordElement(0x032B))), //
				new FC6WriteRegisterTask(0x032C, //
						m(EssSinexcel.ChannelId.DISCHARGE_MAX_A, new UnsignedWordElement(0x032C))), //

				new FC6WriteRegisterTask(0x0329,
						m(EssSinexcel.ChannelId.SET_SLOW_CHARGE_VOLTAGE, new UnsignedWordElement(0x0329))),
				new FC6WriteRegisterTask(0x0328,
						m(EssSinexcel.ChannelId.SET_FLOAT_CHARGE_VOLTAGE, new UnsignedWordElement(0x0328))),

				new FC6WriteRegisterTask(0x032E,
						m(EssSinexcel.ChannelId.CHARGE_MAX_V, new UnsignedWordElement(0x032E))),
				new FC6WriteRegisterTask(0x032D,
						m(EssSinexcel.ChannelId.DISCHARGE_MIN_V, new UnsignedWordElement(0x032D))),

				new FC16WriteRegistersTask(0x007E,
						m(EssSinexcel.ChannelId.SET_ANALOG_CHARGE_ENERGY, new UnsignedDoublewordElement(0x007E))),
				// new FC6WriteRegisterTask(0x007F,
				// m(EssSinexcel.ChannelId.SET_ANALOG_CHARGE_ENERGY, new
				// UnsignedWordElement(0x007F))),

				new FC16WriteRegistersTask(0x0080,
						m(EssSinexcel.ChannelId.SET_ANALOG_DISCHARGE_ENERGY, new UnsignedDoublewordElement(0x0080))),
				// new FC6WriteRegisterTask(0x0081,
				// m(EssSinexcel.ChannelId.SET_ANALOG_DISCHARGE_ENERGY, new
				// UnsignedWordElement(0x0081))),

				new FC16WriteRegistersTask(0x0090,
						m(EssSinexcel.ChannelId.SET_ANALOG_DC_CHARGE_ENERGY, new UnsignedDoublewordElement(0x0090))),
				// new FC6WriteRegisterTask(0x0091,
				// m(EssSinexcel.ChannelId.SET_ANALOG_DC_CHARGE_ENERGY, new
				// UnsignedWordElement(0x0091))),

				new FC16WriteRegistersTask(0x0092,
						m(EssSinexcel.ChannelId.SET_ANALOG_DC_DISCHARGE_ENERGY, new UnsignedDoublewordElement(0x0092))),
				// new FC6WriteRegisterTask(0x0093,
				// m(EssSinexcel.ChannelId.SET_ANALOG_DC_DISCHARGE_ENERGY, new
				// UnsignedWordElement(0x0093))),

				new FC3ReadRegistersTask(0x0001, Priority.ONCE, //
						m(EssSinexcel.ChannelId.MODEL, new StringWordElement(0x0001, 16)), //
						m(EssSinexcel.ChannelId.SERIAL, new StringWordElement(0x0011, 8))), //

				new FC3ReadRegistersTask(0x0065, Priority.LOW, //
						m(EssSinexcel.ChannelId.INVOUTVOLT_L1, new UnsignedWordElement(0x0065),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(EssSinexcel.ChannelId.INVOUTVOLT_L2, new UnsignedWordElement(0x0066),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(EssSinexcel.ChannelId.INVOUTVOLT_L3, new UnsignedWordElement(0x0067),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(EssSinexcel.ChannelId.INVOUTCURRENT_L1, new UnsignedWordElement(0x0068),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(EssSinexcel.ChannelId.INVOUTCURRENT_L2, new UnsignedWordElement(0x0069),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(EssSinexcel.ChannelId.INVOUTCURRENT_L3, new UnsignedWordElement(0x006A),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						new DummyRegisterElement(0x006B, 0x007D), //
						m(SymmetricEss.ChannelId.ACTIVE_DISCHARGE_ENERGY, new UnsignedDoublewordElement(0x007E),
								ElementToChannelConverter.SCALE_FACTOR_2), //
						m(SymmetricEss.ChannelId.ACTIVE_CHARGE_ENERGY, new UnsignedDoublewordElement(0x0080),
								ElementToChannelConverter.SCALE_FACTOR_2), //
						new DummyRegisterElement(0x0082, 0x0083),
						m(EssSinexcel.ChannelId.TEMPERATURE, new SignedWordElement(0x0084)),
						new DummyRegisterElement(0x0085, 0x008C), //
						m(EssSinexcel.ChannelId.DC_POWER, new SignedWordElement(0x008D),
								ElementToChannelConverter.SCALE_FACTOR_1),
						new DummyRegisterElement(0x008E, 0x008F), //
						m(EssSinexcel.ChannelId.ANALOG_DC_CHARGE_ENERGY, new UnsignedDoublewordElement(0x0090)), //
						m(EssSinexcel.ChannelId.ANALOG_DC_DISCHARGE_ENERGY, new UnsignedDoublewordElement(0x0092))), //

				new FC3ReadRegistersTask(0x0220, Priority.ONCE,
						m(EssSinexcel.ChannelId.VERSION, new StringWordElement(0x0220, 8))), //

				new FC3ReadRegistersTask(0x0248, Priority.HIGH, //
						m(SymmetricEss.ChannelId.ACTIVE_POWER, new SignedWordElement(0x0248), //
								new ElementToChannelConverterChain(
										ElementToChannelConverter.SCALE_FACTOR_1, IGNORE_LESS_THAN_100)),
						new DummyRegisterElement(0x0249),
						m(EssSinexcel.ChannelId.FREQUENCY, new SignedWordElement(0x024A),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_2),
						new DummyRegisterElement(0x024B, 0x024D), //
						m(SymmetricEss.ChannelId.REACTIVE_POWER, new SignedWordElement(0x024E)), //
						new DummyRegisterElement(0x024F, 0x0254), //
						m(EssSinexcel.ChannelId.DC_CURRENT, new SignedWordElement(0x0255),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						new DummyRegisterElement(0x0256), //
						m(EssSinexcel.ChannelId.DC_VOLTAGE, new UnsignedWordElement(0x0257),
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						new DummyRegisterElement(0x0258, 0x0259), //
						m(EssSinexcel.ChannelId.SINEXCEL_STATE, new UnsignedWordElement(0x025A))), //

				new FC3ReadRegistersTask(0x032D, Priority.LOW,
						m(EssSinexcel.ChannelId.LOWER_VOLTAGE_LIMIT, new UnsignedWordElement(0x032D), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1),
						m(EssSinexcel.ChannelId.UPPER_VOLTAGE_LIMIT, new UnsignedWordElement(0x032E), //
								ElementToChannelConverter.SCALE_FACTOR_MINUS_1)),

				new FC3ReadRegistersTask(0x0262, Priority.LOW, //
						m(new BitsWordElement(0x0262, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_0) //
								.bit(1, EssSinexcel.ChannelId.STATE_1) //
								.bit(2, EssSinexcel.ChannelId.STATE_2) //
								.bit(3, EssSinexcel.ChannelId.STATE_3) //
								.bit(4, EssSinexcel.ChannelId.STATE_4) //
								.bit(5, EssSinexcel.ChannelId.STATE_5) //
								.bit(6, EssSinexcel.ChannelId.STATE_6) //
								.bit(7, EssSinexcel.ChannelId.STATE_7) //
								.bit(8, EssSinexcel.ChannelId.STATE_8) //
								.bit(9, EssSinexcel.ChannelId.STATE_9) //
								.bit(10, EssSinexcel.ChannelId.STATE_10) //
								.bit(11, EssSinexcel.ChannelId.STATE_11) //
								.bit(12, EssSinexcel.ChannelId.STATE_12) //
								.bit(13, EssSinexcel.ChannelId.STATE_13) //
								.bit(14, EssSinexcel.ChannelId.STATE_14) //
								.bit(15, EssSinexcel.ChannelId.STATE_15))),

				new FC3ReadRegistersTask(0x0260, Priority.LOW, //
						m(new BitsWordElement(0x0260, this) //
								.bit(1, EssSinexcel.ChannelId.SINEXCEL_STATE_1) //
								.bit(2, EssSinexcel.ChannelId.SINEXCEL_STATE_2) //
								.bit(3, EssSinexcel.ChannelId.SINEXCEL_STATE_3) //
								.bit(4, EssSinexcel.ChannelId.SINEXCEL_STATE_4) //
								.bit(5, EssSinexcel.ChannelId.SINEXCEL_STATE_5) //
								.bit(6, EssSinexcel.ChannelId.SINEXCEL_STATE_6) //
								.bit(7, EssSinexcel.ChannelId.SINEXCEL_STATE_7) //
								.bit(8, EssSinexcel.ChannelId.SINEXCEL_STATE_8) //
								.bit(9, EssSinexcel.ChannelId.SINEXCEL_STATE_9))),

				new FC3ReadRegistersTask(0x0020, Priority.LOW, //
						m(new BitsWordElement(0x0020, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_16) //
								.bit(1, EssSinexcel.ChannelId.STATE_17) //
								.bit(2, EssSinexcel.ChannelId.STATE_18) //
								.bit(3, EssSinexcel.ChannelId.STATE_19) //
								.bit(4, EssSinexcel.ChannelId.STATE_20))),

				new FC3ReadRegistersTask(0x0024, Priority.LOW, //
						m(new BitsWordElement(0x0024, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_21) //
								.bit(1, EssSinexcel.ChannelId.STATE_22) //
								.bit(2, EssSinexcel.ChannelId.STATE_23) //
								.bit(3, EssSinexcel.ChannelId.STATE_24) //
								.bit(4, EssSinexcel.ChannelId.STATE_25) //
								.bit(5, EssSinexcel.ChannelId.STATE_26) //
								.bit(6, EssSinexcel.ChannelId.STATE_27) //
								.bit(7, EssSinexcel.ChannelId.STATE_28) //
								.bit(8, EssSinexcel.ChannelId.STATE_29) //
								.bit(9, EssSinexcel.ChannelId.STATE_30) //
								.bit(10, EssSinexcel.ChannelId.STATE_31) //
								.bit(11, EssSinexcel.ChannelId.STATE_32) //
								.bit(12, EssSinexcel.ChannelId.STATE_33))),

				new FC3ReadRegistersTask(0x0025, Priority.LOW, //
						m(new BitsWordElement(0x0025, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_34) //
								.bit(1, EssSinexcel.ChannelId.STATE_35) //
								.bit(2, EssSinexcel.ChannelId.STATE_36) //
								.bit(3, EssSinexcel.ChannelId.STATE_37) //
								.bit(4, EssSinexcel.ChannelId.STATE_38) //
								.bit(5, EssSinexcel.ChannelId.STATE_39) //
								.bit(6, EssSinexcel.ChannelId.STATE_40) //
								.bit(7, EssSinexcel.ChannelId.STATE_41) //
								.bit(8, EssSinexcel.ChannelId.STATE_42) //
								.bit(9, EssSinexcel.ChannelId.STATE_43) //
								.bit(10, EssSinexcel.ChannelId.STATE_44) //
								.bit(11, EssSinexcel.ChannelId.STATE_45) //
								.bit(13, EssSinexcel.ChannelId.STATE_47) //
								.bit(14, EssSinexcel.ChannelId.STATE_48) //
								.bit(15, EssSinexcel.ChannelId.STATE_49))),

				new FC3ReadRegistersTask(0x0026, Priority.LOW, //
						m(new BitsWordElement(0x0026, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_50) //
								.bit(2, EssSinexcel.ChannelId.STATE_52) //
								.bit(3, EssSinexcel.ChannelId.STATE_53) //
								.bit(4, EssSinexcel.ChannelId.STATE_54))),

				new FC3ReadRegistersTask(0x0027, Priority.LOW, //
						m(new BitsWordElement(0x0027, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_55) //
								.bit(1, EssSinexcel.ChannelId.STATE_56) //
								.bit(2, EssSinexcel.ChannelId.STATE_57) //
								.bit(3, EssSinexcel.ChannelId.STATE_58))),

				new FC3ReadRegistersTask(0x0028, Priority.LOW, //
						m(new BitsWordElement(0x0028, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_59) //
								.bit(1, EssSinexcel.ChannelId.STATE_60) //
								.bit(2, EssSinexcel.ChannelId.STATE_61) //
								.bit(3, EssSinexcel.ChannelId.STATE_62) //
								.bit(4, EssSinexcel.ChannelId.STATE_63) //
								.bit(5, EssSinexcel.ChannelId.STATE_64))),

				new FC3ReadRegistersTask(0x002B, Priority.LOW, //
						m(new BitsWordElement(0x002B, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_65) //
								.bit(1, EssSinexcel.ChannelId.STATE_66) //
								.bit(2, EssSinexcel.ChannelId.STATE_67) //
								.bit(3, EssSinexcel.ChannelId.STATE_68))),

				new FC3ReadRegistersTask(0x002C, Priority.LOW, //
						m(new BitsWordElement(0x002C, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_69) //
								.bit(1, EssSinexcel.ChannelId.STATE_70) //
								.bit(2, EssSinexcel.ChannelId.STATE_71) //
								.bit(3, EssSinexcel.ChannelId.STATE_72) //
								.bit(4, EssSinexcel.ChannelId.STATE_73))),

				new FC3ReadRegistersTask(0x002F, Priority.LOW, //
						m(new BitsWordElement(0x002F, this) //
								.bit(0, EssSinexcel.ChannelId.STATE_74))));
	}

	@Override
	public String debugLog() {
		return "SoC:" + this.getSoc().asString() //
				+ "|L:" + this.getActivePower().asString() //
				+ "|Allowed:"
				+ this.channel(ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER).value().asStringWithoutUnit() + ";"
				+ this.channel(ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER).value().asString() //
				+ "|" + this.getGridModeChannel().value().asOptionString();
	}

	@Override
	public Constraint[] getStaticConstraints() throws OpenemsNamedException {
		if (battery.getStartStop() != StartStop.START) {
			return new Constraint[] { //
					this.createPowerConstraint("Battery is not ready", Phase.ALL, Pwr.ACTIVE, Relationship.EQUALS, 0), //
					this.createPowerConstraint("Battery is not ready", Phase.ALL, Pwr.REACTIVE, Relationship.EQUALS, 0) //
			};
		} else {
			return Power.NO_CONSTRAINTS;
		}
	}

	@Override
	public void applyPower(int activePower, int reactivePower) throws OpenemsNamedException {
		switch (this.inverterState) {
		case ON:
			IntegerWriteChannel setActivePower = this.channel(EssSinexcel.ChannelId.SET_ACTIVE_POWER);
			setActivePower.setNextWriteValue(activePower / 100);

			IntegerWriteChannel setReactivePower = this.channel(EssSinexcel.ChannelId.SET_REACTIVE_POWER);
			setReactivePower.setNextWriteValue(reactivePower / 100);

			boolean isOn = this.stateOnOff();
			if (activePower == 0 && reactivePower == 0 && isOn) {
				this.counterOff++;
				if (this.counterOff == 48) {
					this.inverterOff();
					this.counterOff = 0;
				}

			} else if ((activePower != 0 || reactivePower != 0) && !isOn) {
				this.inverterOn();
			}
			break;

		case OFF:
			if (this.stateOnOff() == true) {
				this.inverterOff();
			} else {
				return;
			}
			break;
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
//		boolean island = faultIslanding();
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
			try {
				this.setBatteryRanges();
				this.doHandlingSlowFloatVoltage();
				this.stateMachine.run();
			} catch (OpenemsNamedException e) {
				this.logError(this.log, "EventHandler failed: " + e.getMessage());
			}

//			if(island = true) {
//				islandingOn();
//			}
//			else if(island = false) {
//				islandingOff();
//			}

			break;
		}

	}

	@Override
	public Power getPower() {
		return this.power;
	}

	@Override
	public int getPowerPrecision() {
		return 100;
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable( //
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				SymmetricEss.getModbusSlaveNatureTable(accessMode), //
				ManagedSymmetricEss.getModbusSlaveNatureTable(accessMode) //
		);
	}

	/**
	 * The Sinexcel Battery Inverter claims to outputting a little bit of power even
	 * if it does not. This little filter ignores values for ActivePower less than
	 * 100 (charge/discharge).
	 */
	private static final ElementToChannelConverter IGNORE_LESS_THAN_100 = new ElementToChannelConverter(//
			obj -> {
				if (obj == null) {
					return null;
				}
				int value = (Short) obj;
				if (Math.abs(value) < 100) {
					return 0;
				} else {
					return value;
				}
			}, //
			value -> value);

}

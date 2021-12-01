package io.openems.edge.controller.evcs;

import java.io.IOException;
import java.time.Clock;
import java.util.Dictionary;
import java.util.Optional;

import org.osgi.service.cm.Configuration;
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
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.sum.Sum;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Power;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.evcs.api.Evcs;
import io.openems.edge.evcs.api.ManagedEvcs;
import io.openems.edge.evcs.api.Status;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.Evcs", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class EvcsController extends AbstractOpenemsComponent implements Controller, OpenemsComponent, ModbusSlave {

	private final Logger log = LoggerFactory.getLogger(EvcsController.class);
	private static final int CHARGE_POWER_BUFFER = 200;
	private static final double DEFAULT_UPPER_TARGET_DIFFERENCE_PERCENT = 0.10; // 10%

	private final ChargingLowerThanTargetHandler chargingLowerThanTargetHandler;

	private Config config;

	@Reference
	protected ComponentManager componentManager;

	@Reference
	protected ConfigurationAdmin cm;

	@Reference
	protected Sum sum;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private ManagedEvcs evcs;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private SymmetricEss ess;

	public EvcsController() {
		this(Clock.systemDefaultZone());
	}

	protected EvcsController(Clock clock) {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values() //
		);
		this.chargingLowerThanTargetHandler = new ChargingLowerThanTargetHandler(this);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		if (config.forceChargeMinPower() < 0) {
			throw new OpenemsException("Force-Charge Min-Power [" + config.forceChargeMinPower() + "] must be >= 0");
		}

		if (config.defaultChargeMinPower() < 0) {
			throw new OpenemsException(
					"Default-Charge Min-Power [" + config.defaultChargeMinPower() + "] must be >= 0");
		}

		this.config = config;

		if (OpenemsComponent.updateReferenceFilter(cm, this.servicePid(), "evcs", config.evcs_id())) {
			return;
		}
		if (OpenemsComponent.updateReferenceFilter(cm, this.servicePid(), "ess", config.ess_id())) {
			return;
		}
		this.evcs._setMaximumPower(null);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	/**
	 * If the EVCS is clustered the method will set the charge power request.
	 * Otherwise it will set directly the charge power limit.
	 */
	@Override
	public void run() throws OpenemsNamedException {

		boolean isClustered = this.evcs.getIsClustered().orElse(false);

		/*
		 * Stop early if charging is disabled
		 */
		if (!this.config.enabledCharging()) {
			this.evcs.setChargePowerLimit(0);
			if (isClustered) {
				this.evcs.setChargePowerRequest(0);
				this.resetMinMaxChannels();
			}
			return;
		}

		adaptConfigToHardwareLimits();

		this.evcs.setEnergyLimit(this.config.energySessionLimit());

		/*
		 * Sets a fixed request of 0 if the Charger is not ready
		 */
		if (isClustered) {

			Status status = this.evcs.getStatus();
			switch (status) {
			case ERROR:
			case STARTING:
			case UNDEFINED:
			case NOT_READY_FOR_CHARGING:
			case ENERGY_LIMIT_REACHED:
				this.evcs.setChargePowerRequest(0);
				resetMinMaxChannels();
				return;
			case CHARGING_REJECTED:
			case READY_FOR_CHARGING:
			case CHARGING_FINISHED:
				this.evcs._setMaximumPower(null);
			case CHARGING:
				break;
			}
		}

		int nextChargePower = 0;
		int nextMinPower = 0;

		/*
		 * Calculates the next charging power depending on the charge mode
		 */
		switch (config.chargeMode()) {
		case EXCESS_POWER:
			/*
			 * Get the next charge power depending on the priority.
			 */
			switch (config.priority()) {
			case CAR:
				nextChargePower = this.calculateChargePowerFromExcessPower(this.evcs);
				break;

			case STORAGE:
				int storageSoc = this.sum.getEssSoc().orElse(0);
				if (storageSoc > 97) {
					nextChargePower = this.calculateChargePowerFromExcessPower(this.evcs);
				} else {
					nextChargePower = this.calculateExcessPowerAfterEss(this.evcs, this.ess);
				}
				break;
			}

			Channel<Integer> minimumHardwarePowerChannel = evcs.channel(Evcs.ChannelId.MINIMUM_HARDWARE_POWER);
			if (nextChargePower < minimumHardwarePowerChannel.value()
					.orElse(0)) { /* charging under 6A isn't possible */
				nextChargePower = 0;
			}

			this.evcs._setMinimumPower(config.defaultChargeMinPower());
			nextMinPower = config.defaultChargeMinPower();
			break;

		case FORCE_CHARGE:
			this.evcs._setMinimumPower(0);
			nextChargePower = config.forceChargeMinPower() * this.evcs.getPhases().orElse(3);
			break;
		}

		if (nextChargePower < nextMinPower) {
			nextChargePower = nextMinPower;
		}

		// charging under minimum hardware power isn't possible
		Channel<Integer> minimumHardwarePowerChannel = evcs.channel(Evcs.ChannelId.MINIMUM_HARDWARE_POWER);
		if (nextChargePower < minimumHardwarePowerChannel.value().orElse(0)) {
			nextChargePower = 0;
		}

		/**
		 * Calculates the maximum Power of the Car.
		 */
		if (nextChargePower != 0) {

			int chargePower = this.evcs.getChargePower().orElse(0);

			/**
			 * Check the difference of the current charge power and the previous charging
			 * target
			 */
			if (this.chargingLowerThanTargetHandler.isLower(this.evcs)) {

				Integer maximumPower = this.chargingLowerThanTargetHandler.getMaximumChargePower();
				if (maximumPower != null) {
					this.evcs._setMaximumPower(maximumPower + CHARGE_POWER_BUFFER);
					this.logDebug(this.log,
							"Maximum Charge Power of the EV reduced to" + maximumPower + " W plus buffer");
				}
			} else {
				int currMax = this.evcs.getMaximumPower().orElse(0);

				/**
				 * If the charge power would increases again above the current maximum power, it
				 * resets the maximum Power.
				 */
				if (chargePower > currMax * (1 + DEFAULT_UPPER_TARGET_DIFFERENCE_PERCENT)) {
					this.evcs._setMaximumPower(null);
				}
			}
		}

		if (isClustered) {
			this.evcs.setChargePowerRequest(nextChargePower);
		} else {
			this.evcs.setChargePowerLimit(nextChargePower);
		}
		this.logDebug(this.log, "Next charge power: " + nextChargePower + " W");
	}

	/**
	 * Resetting the minimum and maximum power channels.
	 */
	private void resetMinMaxChannels() {
		evcs._setMinimumPower(0);
		evcs._setMaximumPower(null);
	}

	/**
	 * Adapt the charge limits to the given hardware limits of the EVCS.
	 */
	private void adaptConfigToHardwareLimits() {

		Optional<Integer> maxHardwareOpt = this.evcs.getMaximumHardwarePower().asOptional();
		if (maxHardwareOpt.isPresent()) {
			int maxHW = maxHardwareOpt.get();
			if (maxHW != 0) {
				maxHW = (int) Math.ceil(maxHW / 100.0) * 100;
				if (config.defaultChargeMinPower() > maxHW) {
					configUpdate("defaultChargeMinPower", maxHW);
				}
			}
		}

	}

	/**
	 * Calculates the next charging power, depending on the current PV production
	 * and house consumption.
	 * 
	 * @param evcs Electric Vehicle Charging Station
	 * @return the available excess power for charging
	 * @throws OpenemsNamedException on error
	 */
	private int calculateChargePowerFromExcessPower(ManagedEvcs evcs) throws OpenemsNamedException {

		int buyFromGrid = this.sum.getGridActivePower().orElse(0);
		int essDischarge = this.sum.getEssActivePower().orElse(0);
		int essActivePowerDC = this.sum.getProductionDcActualPower().orElse(0);
		int evcsCharge = evcs.getChargePower().orElse(0);

		return evcsCharge - buyFromGrid - (essDischarge - essActivePowerDC);
	}

	/**
	 * Calculates the next charging power from excess power after Ess charging.
	 * 
	 * @param evcs the ManagedEvcs
	 * @param ess  the ManagedSymmetricEss
	 * @return the available excess power for charging
	 */
	private int calculateExcessPowerAfterEss(ManagedEvcs evcs, SymmetricEss ess) {
		int maxEssCharge;
		if (ess instanceof ManagedSymmetricEss) {
			ManagedSymmetricEss e = (ManagedSymmetricEss) ess;
			Power power = ((ManagedSymmetricEss) ess).getPower();
			maxEssCharge = power.getMinPower(e, Phase.ALL, Pwr.ACTIVE);
			maxEssCharge = Math.abs(maxEssCharge);
		} else {
			maxEssCharge = ess.getMaxApparentPower().orElse(0);
		}
		int buyFromGrid = this.sum.getGridActivePower().orElse(0);
		int essActivePower = this.sum.getEssActivePower().orElse(0);
		int essActivePowerDC = this.sum.getProductionDcActualPower().orElse(0);
		int evcsCharge = evcs.getChargePower().orElse(0);
		int result = -buyFromGrid + evcsCharge - (maxEssCharge + (essActivePower - essActivePowerDC));
		result = result > 0 ? result : 0;

		return result;
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable(//
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				Controller.getModbusSlaveNatureTable(accessMode));
	}

	/**
	 * Updating the configuration property to given value.
	 * 
	 * @param targetProperty Property that should be changed
	 * @param requiredValue  Value that should be set
	 */
	public void configUpdate(String targetProperty, Object requiredValue) {

		Configuration c;
		try {
			String pid = this.servicePid();
			if (pid.isEmpty()) {
				this.logInfo(log, "PID of " + this.id() + " is Empty");
				return;
			}
			c = cm.getConfiguration(pid, "?");
			Dictionary<String, Object> properties = c.getProperties();
			Object target = properties.get(targetProperty);
			String existingTarget = target.toString();
			if (!existingTarget.isEmpty()) {
				properties.put(targetProperty, requiredValue);
				c.update(properties);
			}
		} catch (IOException | SecurityException e) {
			this.logError(log, "ERROR: " + e.getMessage());
		}
	}

	@Override
	public void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	protected void logWarn(Logger log, String message) {
		super.logWarn(log, message);
	}

	@Override
	protected void logDebug(Logger log, String message) {
		if (this.config.debugMode()) {
			this.logInfo(this.log, message);
		}
	}
}

package io.openems.edge.controller.timeslotpeakshaving;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.InvalidValueException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Pwr;
//import io.openems.edge.ess.power.api.Phase;
//import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.meter.api.SymmetricMeter;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.TimeslotPeakshaving", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class TimeslotPeakshaving extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	public static final String TIME_FORMAT = "HH:mm";
	public static final String DATE_FORMAT = "dd.MM.yyyy";

	private final Logger log = LoggerFactory.getLogger(TimeslotPeakshaving.class);

	@Reference
	protected ComponentManager componentManager;

	private Config config;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalTime startTime;
	private LocalTime endTime;
	private LocalTime slowStartTime;
	private int slowforceChargeMinutes;
	private ChargeState chargeState = ChargeState.NORMAL;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		STATE_MACHINE(Doc.of(ChargeState.values()) //
				.text("Current State of State-Machine")),
		CALCULATED_POWER(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.WATT));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	public TimeslotPeakshaving() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		this.startDate = convertDate(config.startDate());
		this.endDate = convertDate(config.endDate());
		this.startTime = convertTime(config.startTime());
		this.endTime = convertTime(config.endTime());
		this.slowStartTime = convertTime(config.slowChargeStartTime());
		this.slowforceChargeMinutes = calculateSlowForceChargeMinutes(this.slowStartTime, this.startTime);
		this.config = config;

		super.activate(context, config.id(), config.alias(), config.enabled());
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		ManagedSymmetricEss ess = this.componentManager.getComponent(this.config.ess());
		SymmetricMeter meter = this.componentManager.getComponent(this.config.meter_id());

		Integer power = this.getPower(ess, meter);
		this.applyPower(ess, power);
	}

	/**
	 * Applies the power on the Ess.
	 * 
	 * @param ess {@link ManagedSymmetricEss} where the power needs to be set
	 * @throws OpenemsNamedException on error
	 */
	private void applyPower(ManagedSymmetricEss ess, Integer activePower) throws OpenemsNamedException {
		if (activePower != null) {
			ess.setActivePowerEqualsWithPid(activePower);
			ess.setReactivePowerEquals(0);
		}
	}

	/**
	 * Gets the current ActivePower.
	 * 
	 * @return the currently valid active power, or null to set no power
	 * @throws IllegalArgumentException on error
	 * @throws OpenemsException         on error
	 */
	private Integer getPower(ManagedSymmetricEss ess, SymmetricMeter meter)
			throws OpenemsException, IllegalArgumentException {

		LocalDateTime now = LocalDateTime.now(this.componentManager.getClock());

		boolean stateChanged;
		Integer power = null;

		do {
			stateChanged = false;
			switch (this.chargeState) {
			case NORMAL:
				if (this.isHighLoadTimeslot(now.plusMinutes(this.slowforceChargeMinutes))) {
					stateChanged = changeState(ChargeState.SLOWCHARGE);
				}
				if (this.isHighLoadTimeslot(now)) {
					stateChanged = changeState(ChargeState.HIGHTHRESHOLD_TIMESLOT);
				}

				power = null;
				break;
			case SLOWCHARGE:
				if (this.isHighLoadTimeslot(now)) {
					stateChanged = changeState(ChargeState.HIGHTHRESHOLD_TIMESLOT);
				}

				int minPower = ess.getPower().getMinPower(ess, Phase.ALL, Pwr.ACTIVE);
				if (ess.getSoc().orElse(0) == 100 || minPower >= 0) {
					// no need to charge anymore, the soc would be 100 %
					stateChanged = changeState(ChargeState.HYSTERESIS);
				}
				power = config.slowChargePower();
				break;
			case HYSTERESIS:
				if (ess.getSoc().orElse(0) <= config.hysteresisSoc()) {
					stateChanged = changeState(ChargeState.SLOWCHARGE);
				}
				if (this.isHighLoadTimeslot(now)) {
					stateChanged = changeState(ChargeState.HIGHTHRESHOLD_TIMESLOT);
				}
				power = null;
				break;
			case HIGHTHRESHOLD_TIMESLOT:
				if (!this.isHighLoadTimeslot(now)) {
					stateChanged = changeState(ChargeState.NORMAL);
				}

				power = this.calculatePeakShavePower(ess, meter);
				break;
			}
		} while (stateChanged); // execute again if the state changed

		// store current state in StateMachine channel
		this.channel(ChannelId.STATE_MACHINE).setNextValue(this.chargeState);
		return power;

	}

	/**
	 * A flag to maintain change in the state.
	 * 
	 * @param nextState the target state
	 * @return Flag that the state is changed or not
	 */
	private boolean changeState(ChargeState nextState) {
		if (this.chargeState != nextState) {
			this.chargeState = nextState;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method calculates the power that is required to cut the peak during
	 * timeslot.
	 * 
	 * @param ess   the {@link ManagedSymmetricEss}
	 * @param meter the {@link SymmetricMeter} of the grid
	 * @return activepower to be set on the ess
	 * @throws InvalidValueException on error
	 */
	private int calculatePeakShavePower(ManagedSymmetricEss ess, SymmetricMeter meter) throws InvalidValueException {
		/*
		 * Check that we are On-Grid (and warn on undefined Grid-Mode)
		 */
		GridMode gridMode = ess.getGridMode();
		switch (gridMode) {
		case UNDEFINED:
			this.logWarn(this.log, "Grid-Mode is [UNDEFINED]");
			break;
		case ON_GRID:
			break;
		case OFF_GRID:
			return 0;
		}

		// Calculate 'real' grid-power (without current ESS charge/discharge)
		int gridPower = meter.getActivePower().getOrError() /* current buy-from/sell-to grid */
				+ ess.getActivePower().getOrError() /* current charge/discharge Ess */;

		int calculatedPower;
		if (gridPower >= config.peakShavingPower()) {
			/*
			 * Peak-Shaving
			 */
			calculatedPower = gridPower -= config.peakShavingPower();

		} else if (gridPower <= config.rechargePower()) {
			/*
			 * Recharge
			 */
			calculatedPower = gridPower -= config.rechargePower();

		} else {
			/*
			 * Set no charge/discharge
			 */
			calculatedPower = 0;
		}
		this.channel(ChannelId.CALCULATED_POWER).setNextValue(calculatedPower);
		return calculatedPower;
	}

	/**
	 * Is the current time in a high-load timeslot?.
	 * 
	 * @param dateTime the datetime to be checked
	 * @return boolean true if time is within timeslot
	 * @throws OpenemsException on error
	 */
	private boolean isHighLoadTimeslot(LocalDateTime dateTime) throws OpenemsException {

		if (!isConfiguredActiveDay(this.config)) {
			return false;
		}
		if (!isActiveDate(this.startDate, this.endDate, dateTime)) {
			return false;
		}
		if (!isActiveTime(this.startTime, this.endTime, dateTime)) {
			return false;
		}
		// all tests passed
		return true;
	}

	/**
	 * Is "day" configured to run algorithm?.
	 * 
	 * @param config the configuration
	 * @return configuredDay boolean value specifying the day is set or not.
	 */
	private static boolean isConfiguredActiveDay(Config config) {

		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		boolean configuredDay = false;

		switch (day) {
		case Calendar.SUNDAY:
			if (config.sunday()) {
				configuredDay = true;
			}
			break;
		case Calendar.MONDAY:
			if (config.monday()) {
				configuredDay = true;
			}
			break;
		case Calendar.TUESDAY:
			if (config.tuesday()) {
				configuredDay = true;
			}
			break;
		case Calendar.WEDNESDAY:
			if (config.wednesday()) {
				configuredDay = true;
			}
			break;
		case Calendar.THURSDAY:
			if (config.thursday()) {
				configuredDay = true;
			}
			break;
		case Calendar.FRIDAY:
			if (config.friday()) {
				configuredDay = true;
			}
			break;
		case Calendar.SATURDAY:
			if (config.saturday()) {
				configuredDay = true;
			}
			break;
		}
		return configuredDay;
	}

	/**
	 * This method returns true if the Current date is within configured StartDate
	 * and endDate.
	 * 
	 * @param startDate the configured start date
	 * @param endDate   the configured end date
	 * @param dateTime  the date to be tested
	 * @return boolean values which specify the current date is within the
	 *         configured date range
	 */
	protected static boolean isActiveDate(LocalDate startDate, LocalDate endDate, LocalDateTime dateTime) {
		LocalDate date = dateTime.toLocalDate();
		return !(date.isBefore(startDate) || date.isAfter(endDate));
	}

	/**
	 * Is the time of 'dateTime' within startTime and endTime?.
	 * 
	 * @param startTime the configured start time
	 * @param endTime   the configured end time
	 * @param dateTime  the time to be tested
	 * @return
	 */
	protected static boolean isActiveTime(LocalTime startTime, LocalTime endTime, LocalDateTime dateTime) {
		LocalTime time = dateTime.toLocalTime();
		return !(time.isBefore(startTime) || time.isAfter(endTime));
	}

	/**
	 * Converts a string to a LocalDate.
	 * 
	 * @param date the date as a String
	 * @return the converted date
	 */
	protected static LocalDate convertDate(String date) throws OpenemsException {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
		return localDate;
	}

	/**
	 * Converts a string to a LocalTime.
	 * 
	 * @param time the time as a string
	 * @return the converted time
	 */
	protected static LocalTime convertTime(String time) throws OpenemsException {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
		LocalTime localDate = LocalTime.parse(time, dateTimeFormatter);
		return localDate;
	}

	/**
	 * This methods calculates the slow charging minutes from slowStartTime and
	 * startTime, this is the period for charging the battery to 100% and getting
	 * ready for highthreshold period.
	 * 
	 * @param slowStartTime start of slow charging the battery
	 * @param startTime     start of the high threshold period
	 * @return forceChargeMinutes in int, which specifies the total time the battery
	 *         should be slowly charged
	 */
	private static int calculateSlowForceChargeMinutes(LocalTime slowStartTime, LocalTime startTime) {
		int forceChargeTime = (int) ChronoUnit.MINUTES.between(slowStartTime, startTime);
		if (forceChargeTime > 0) {
			return forceChargeTime;
		} else {
			return forceChargeTime + 1440; // 1440 - total minutes in a day
		}
	}

}
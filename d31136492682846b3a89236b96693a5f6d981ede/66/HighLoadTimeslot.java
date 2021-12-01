package io.openems.edge.controller.highloadtimeslot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.ess.power.api.Relationship;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.HighLoadTimeslot", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class HighLoadTimeslot extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	public static final String TIME_FORMAT = "HH:mm";
	public static final String DATE_FORMAT = "dd.MM.yyyy";

	/**
	 * This many minutes before the high-load timeslot force charging is activated.
	 */
	private static final int FORCE_CHARGE_MINUTES = 30;

	@Reference
	protected ComponentManager componentManager;

	private final Logger log = LoggerFactory.getLogger(HighLoadTimeslot.class);

	private String essId;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalTime startTime;
	private LocalTime endTime;
	private int chargePower;
	private int dischargePower;
	private int hysteresisSoc;
	private WeekdayFilter weekdayDayFilter;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		// TODO implement State_Machine channel
		;
		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	public HighLoadTimeslot() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		this.essId = config.ess();
		this.startDate = convertDate(config.startDate());
		this.endDate = convertDate(config.endDate());
		this.startTime = convertTime(config.startTime());
		this.endTime = convertTime(config.endTime());
		this.chargePower = config.chargePower();
		this.dischargePower = config.dischargePower();
		this.hysteresisSoc = config.hysteresisSoc();
		this.weekdayDayFilter = config.weekdayFilter();

		super.activate(context, config.id(), config.alias(), config.enabled());
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		ManagedSymmetricEss ess = this.componentManager.getComponent(this.essId);

		int power = getPower(ess);
		this.applyPower(ess, power);
	}

	private ChargeState chargeState = ChargeState.NORMAL;

	/**
	 * Gets the current ActivePower.
	 * 
	 * @return
	 */
	private int getPower(ManagedSymmetricEss ess) {
		LocalDateTime now = LocalDateTime.now(this.componentManager.getClock());
		if (this.isHighLoadTimeslot(now)) {
			/*
			 * We are in a High-Load period -> discharge
			 */
			// reset charge state
			this.chargeState = ChargeState.NORMAL;
			this.logInfo(log, "Within High-Load timeslot. Discharge with [" + this.dischargePower + "]");
			return this.dischargePower;
		} else if (this.isHighLoadTimeslot(now.plusMinutes(FORCE_CHARGE_MINUTES))) {
			/*
			 * We are soon going to be in High-Load period -> activate FORCE_CHARGE mode
			 */
			this.chargeState = ChargeState.FORCE_CHARGE;
		}
		/*
		 * We are in a Charge period
		 */
		switch (this.chargeState) {
		case NORMAL:
			/*
			 * charge with configured charge-power
			 */
			this.logInfo(log, "Outside High-Load timeslot. Charge with [" + this.chargePower + "]");
			int minPower = ess.getPower().getMinPower(ess, Phase.ALL, Pwr.ACTIVE);
			if (minPower >= 0) {
				this.logInfo(log, "Min-Power [" + minPower + " >= 0]. Switch to Charge-Hystereses state.");
				// activate Charge-hysteresis if no charge power (i.e. >= 0) is allowed
				this.chargeState = ChargeState.HYSTERESIS;
			}
			return this.chargePower;

		case HYSTERESIS:
			/*
			 * block charging till configured hysteresisSoc
			 */
			this.logInfo(log, "Outside High-Load timeslot. Charge-Hysteresis-Mode: Block charging.");
			if (ess.getSoc().orElse(0) <= this.hysteresisSoc) {
				this.logInfo(log, "SoC [" + ess.getSoc().orElse(0) + " <= " + this.hysteresisSoc
						+ "]. Switch to Charge-Normal state.");
				this.chargeState = ChargeState.NORMAL;
			}
			return 0;

		case FORCE_CHARGE:
			/*
			 * force full charging just before the high-load timeslot starts
			 */
			this.logInfo(log, "Just before High-Load timeslot. Charge with [" + this.chargePower + "]");
			return this.chargePower;
		}
		// we should never come here...
		return 0;
	}

	/**
	 * Is the current time in a high-load timeslot?
	 * 
	 * @return
	 */
	private boolean isHighLoadTimeslot(LocalDateTime dateTime) {
		if (!isActiveWeekday(this.weekdayDayFilter, dateTime)) {
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
	 * Is 'dateTime' within the ActiveWeekdayFilter?
	 * 
	 * @param activeDayFilter
	 * @param dateTime
	 * @return
	 */
	protected static boolean isActiveWeekday(WeekdayFilter activeDayFilter, LocalDateTime dateTime) {
		switch (activeDayFilter) {
		case EVERDAY:
			return true;
		case ONLY_WEEKDAYS:
			return !isWeekend(dateTime);
		case ONLY_WEEKEND:
			return isWeekend(dateTime);
		}
		// should never happen
		return false;
	}

	protected static boolean isActiveDate(LocalDate startDate, LocalDate endDate, LocalDateTime dateTime) {
		LocalDate date = dateTime.toLocalDate();
		return !(date.isBefore(startDate) || date.isAfter(endDate));
	}

	/**
	 * Is the time of 'dateTime' within startTime and endTime?
	 * 
	 * @param startTime
	 * @param endTime
	 * @param dateTime
	 * @return
	 */
	protected static boolean isActiveTime(LocalTime startTime, LocalTime endTime, LocalDateTime dateTime) {
		LocalTime time = dateTime.toLocalTime();
		return !(time.isBefore(startTime) || time.isAfter(endTime));
	}

	/**
	 * Converts a string to a LocalDate.
	 * 
	 * @param date
	 * @return
	 */
	protected static LocalDate convertDate(String date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
		return localDate;
	}

	/**
	 * Converts a string to a LocalTime.
	 * 
	 * @param time
	 * @return
	 */
	protected static LocalTime convertTime(String time) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
		LocalTime localDate = LocalTime.parse(time, dateTimeFormatter);
		return localDate;
	}

	/**
	 * Is 'dateTime' a Saturday or Sunday?
	 * 
	 * @param dateTime
	 * @return
	 */
	protected static boolean isWeekend(LocalDateTime dateTime) {
		DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
		return (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
	}

	/**
	 * Applies the power constraint on the Ess
	 * 
	 * @param activePower
	 * @throws OpenemsException
	 */
	private void applyPower(ManagedSymmetricEss ess, int activePower) throws OpenemsException {
		// adjust value so that it fits into Min/MaxActivePower
		int calculatedPower = ess.getPower().fitValueIntoMinMaxPower(this.id(), ess, Phase.ALL, Pwr.ACTIVE,
				activePower);
		if (calculatedPower != activePower) {
			this.logInfo(log, "- Applying [" + calculatedPower + " W] instead of [" + activePower + "] W");
		}

		// set result
		ess.addPowerConstraintAndValidate("HighLoadTimeslot P", Phase.ALL, Pwr.ACTIVE, Relationship.EQUALS,
				calculatedPower); //
		ess.addPowerConstraintAndValidate("HighLoadTimeslot Q", Phase.ALL, Pwr.REACTIVE, Relationship.EQUALS, 0);
	}

}

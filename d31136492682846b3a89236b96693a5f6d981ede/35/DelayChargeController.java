package io.openems.edge.controller.ess.delaycharge;

import java.time.LocalDateTime;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.EnumReadChannel;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.Ess.DelayCharge", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class DelayChargeController extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	@Reference
	protected ComponentManager componentManager;

	private Config config = null;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		STATE_MACHINE(Doc.of(State.values()) //
				.text("Current State of State-Machine")),
		CHARGE_POWER_LIMIT(Doc.of(OpenemsType.INTEGER) //
				.text("Charge-Power limitation"));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	public DelayChargeController() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		// Get required variables
		ManagedSymmetricEss ess = this.componentManager.getComponent(this.config.ess_id());
		int capacity = ess.getCapacity().getOrError();
		int targetSecondOfDay = this.config.targetHour() * 3600;

		// calculate remaining capacity in Ws
		int remainingCapacity = capacity * (100 - ess.getSoc().getOrError()) * 36;

		// No remaining capacity -> no restrictions
		if (remainingCapacity < 0) {
			this.setChannels(State.NO_REMAINING_CAPACITY, 0);
			return;
		}

		// calculate remaining time
		int remainingTime = targetSecondOfDay - currentSecondOfDay();

		// We already passed the "target hour of day" -> no restrictions
		if (remainingTime < 0) {
			this.setChannels(State.PASSED_TARGET_HOUR, 0);
			return;
		}

		// calculate charge power limit
		int limit = remainingCapacity / remainingTime * -1;

		// reduce limit to MaxApparentPower to avoid very high values in the last
		// seconds
		limit = Math.min(limit, ess.getMaxApparentPower().orElse(0));

		// set ActiveLimit channel
		setChannels(State.ACTIVE_LIMIT, limit * -1);

		// Set limitation for ChargePower
		ess.setActivePowerGreaterOrEquals(limit);
	}

	private int currentSecondOfDay() {
		LocalDateTime now = LocalDateTime.now(this.componentManager.getClock());
		return now.getHour() * 3600 + now.getMinute() * 60 + now.getSecond();
	}

	private void setChannels(State state, int limit) {
		EnumReadChannel stateMachineChannel = this.channel(ChannelId.STATE_MACHINE);
		stateMachineChannel.setNextValue(state);

		IntegerReadChannel chargePowerLimitChannel = this.channel(ChannelId.CHARGE_POWER_LIMIT);
		chargePowerLimitChannel.setNextValue(limit);
	}
}

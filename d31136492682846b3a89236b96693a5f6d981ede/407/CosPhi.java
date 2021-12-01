package io.openems.edge.controller.asymmetric.balancingcosphi;

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
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedAsymmetricEss;
import io.openems.edge.ess.power.api.Constraint;
import io.openems.edge.ess.power.api.LinearCoefficient;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Power;
import io.openems.edge.ess.power.api.PowerException;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.ess.power.api.Relationship;
import io.openems.edge.meter.api.AsymmetricMeter;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.Asymmetric.BalancingCosPhi", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class CosPhi extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	public final static CosPhiDirection DEFAULT_DIRECTION = CosPhiDirection.CAPACITIVE;
	public final static double DEFAULT_COS_PHI = 1d;

	private final Logger log = LoggerFactory.getLogger(CosPhi.class);

	@Reference
	protected ComponentManager componentManager;

	private String essId;
	private String meterId;
	private CosPhiDirection direction = DEFAULT_DIRECTION;
	private double cosPhi = DEFAULT_COS_PHI;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
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

	public CosPhi() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.essId = config.ess_id();
		this.meterId = config.meter_id();
		this.direction = config.direction();
		this.cosPhi = Math.abs(config.cosPhi());
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		AsymmetricMeter meter = this.componentManager.getComponent(this.meterId);
		ManagedAsymmetricEss ess = this.componentManager.getComponent(this.essId);

		this.addConstraint(ess, Phase.L1, meter.getActivePowerL1(), meter.getReactivePowerL1(), ess.getActivePowerL1(),
				ess.getReactivePowerL1());
		this.addConstraint(ess, Phase.L2, meter.getActivePowerL2(), meter.getReactivePowerL2(), ess.getActivePowerL2(),
				ess.getReactivePowerL2());
		this.addConstraint(ess, Phase.L3, meter.getActivePowerL3(), meter.getReactivePowerL3(), ess.getActivePowerL3(),
				ess.getReactivePowerL3());
	}

	private void addConstraint(ManagedAsymmetricEss ess, Phase phase, Value<Integer> meterActivePower,
			Value<Integer> meterReactivePower, Value<Integer> essActivePower, Value<Integer> essReactivePower)
			throws OpenemsException {
		// Calculate the startpoint of the cosPhi line in relation to the ess zero power
		long pNull = meterActivePower.getOrError() + essActivePower.getOrError();
		long qNull = meterReactivePower.getOrError() + essReactivePower.getOrError();
		double m = Math.tan(Math.acos(Math.abs(cosPhi)));
		if (this.direction == CosPhiDirection.INDUCTIVE) {
			m *= -1;
		}
		System.out.println("Steigung [" + m + "] pNull [" + pNull + "] qNull [" + qNull + "]");

		double staticValueOfEquation = m * pNull * qNull;

		try {
			Power power = ess.getPower();
			Constraint c = new Constraint(ess.id() + phase + ": CosPhi [" + cosPhi + "]", new LinearCoefficient[] { //
					new LinearCoefficient(power.getCoefficient(ess, phase, Pwr.ACTIVE), m), //
					new LinearCoefficient(power.getCoefficient(ess, phase, Pwr.REACTIVE), 1) //
			}, Relationship.EQUALS, staticValueOfEquation);
			System.out.println("Add CosPhi: " + c);
			power.addConstraintAndValidate(c);
		} catch (PowerException e) {
			this.logError(this.log, e.getMessage());
		}
	}
}

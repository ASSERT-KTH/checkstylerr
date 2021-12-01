package io.openems.edge.pvinverter.kaco.blueplanet;

import java.util.Map;

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

import com.google.common.collect.ImmutableMap;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.sunspec.DefaultSunSpecModel;
import io.openems.edge.bridge.modbus.sunspec.SunSpecModel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.meter.api.SymmetricMeter;
import io.openems.edge.pvinverter.api.ManagedSymmetricPvInverter;
import io.openems.edge.pvinverter.sunspec.AbstractSunSpecPvInverter;
import io.openems.edge.pvinverter.sunspec.SunSpecPvInverter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "PV-Inverter.KACO.blueplanet", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE, //
				"type=PRODUCTION" //
		})
public class KacoBlueplanet extends AbstractSunSpecPvInverter
		implements SunSpecPvInverter, ManagedSymmetricPvInverter, SymmetricMeter, OpenemsComponent, EventHandler {

	private static final Map<SunSpecModel, Priority> ACTIVE_MODELS = ImmutableMap.<SunSpecModel, Priority>builder()
			.put(DefaultSunSpecModel.S_1, Priority.LOW) // from 40002
			.put(DefaultSunSpecModel.S_103, Priority.HIGH) // from 40070
			.put(DefaultSunSpecModel.S_120, Priority.LOW) // from 40184
			.put(DefaultSunSpecModel.S_121, Priority.LOW) // from 40212
			.put(DefaultSunSpecModel.S_122, Priority.LOW) // from 40244
			.put(DefaultSunSpecModel.S_123, Priority.LOW) // from 40290
			.build();

	// Further available SunSpec blocks provided by KACO blueplanet are:
	// .put(DefaultSunSpecModel.S_113, Priority.LOW) // from 40122
	// .put(DefaultSunSpecModel.S_126, Priority.LOW) // from 40316
	// .put(DefaultSunSpecModel.S_129, Priority.LOW) // from 40544
	// .put(DefaultSunSpecModel.S_130, Priority.LOW) // from 40606
	// .put(DefaultSunSpecModel.S_130, Priority.LOW) // from 40606
	// .put(DefaultSunSpecModel.S_135, Priority.LOW) // from 40668
	// .put(DefaultSunSpecModel.S_136, Priority.LOW) // from 40730
	// .put(DefaultSunSpecModel.S_160, Priority.LOW) // from 40792
	// .put(SunSpecModel.S_64204, Priority.LOW) // from 40842

	private final static int UNIT_ID = 1;
	private final static int READ_FROM_MODBUS_BLOCK = 1;

	@Reference
	protected ConfigurationAdmin cm;

	public KacoBlueplanet() throws OpenemsException {
		super(//
				ACTIVE_MODELS, //
				OpenemsComponent.ChannelId.values(), //
				SymmetricMeter.ChannelId.values(), //
				ManagedSymmetricPvInverter.ChannelId.values(), //
				SunSpecPvInverter.ChannelId.values() //
		);
	}

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		if (super.activate(context, config.id(), config.alias(), config.enabled(), UNIT_ID, this.cm, "Modbus",
				config.modbus_id(), READ_FROM_MODBUS_BLOCK)) {
			return;
		}
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
	}
}

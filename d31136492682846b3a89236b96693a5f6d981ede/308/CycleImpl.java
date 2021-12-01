package io.openems.edge.core.cycle;

import java.util.Comparator;
import java.util.TreeSet;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;

import io.openems.common.OpenemsConstants;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.cycle.Cycle;
import io.openems.edge.common.sum.Sum;
import io.openems.edge.scheduler.api.Scheduler;

@Designate(ocd = Config.class, factory = false)
@Component(//
		name = "Core.Cycle", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.OPTIONAL, //
		property = { //
				"id=" + OpenemsConstants.CYCLE_ID, //
				"enabled=true" //
		})
public class CycleImpl extends AbstractOpenemsComponent implements OpenemsComponent, Cycle {

	private final CycleWorker worker = new CycleWorker(this);

	@Reference
	protected EventAdmin eventAdmin;

	@Reference
	protected Sum sumComponent;

	@Reference
	protected ComponentManager componentManager;

	/**
	 * Holds the Schedulers and their relative cycleTime. They are sorted ascending
	 * by their cycleTimes.
	 */
	protected final TreeSet<Scheduler> schedulers = new TreeSet<Scheduler>(Comparator.comparing(Scheduler::id));

	private Config config = null;;

	@Reference(//
			policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY, //
			cardinality = ReferenceCardinality.MULTIPLE, //
			target = "(enabled=true)")
	protected void addScheduler(Scheduler newScheduler) {
		synchronized (this.schedulers) {
			this.schedulers.add(newScheduler);
		}
	}

	protected void removeScheduler(Scheduler scheduler) {
		synchronized (this.schedulers) {
			this.schedulers.remove(scheduler);
		}
	}

	public CycleImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Cycle.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, OpenemsConstants.CYCLE_ID, "Core.Cycle", true);
		this.config = config;
		this.worker.activate("Core.Cycle");
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
		this.worker.deactivate();
	}

	@Modified
	void modified(ComponentContext context, Config config) throws OpenemsNamedException {
		super.modified(context, OpenemsConstants.CYCLE_ID, "Core.Cycle", true);
		Config oldConfig = this.config;
		this.config = config;
		// make sure the worker starts if it had been stopped
		if (oldConfig.cycleTime() <= 0 && oldConfig.cycleTime() != config.cycleTime()) {
			this.worker.triggerNextRun();
		}
	}

	@Override
	protected void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	protected void logWarn(Logger log, String message) {
		super.logWarn(log, message);
	}

	@Override
	public int getCycleTime() {
		Config config = this.config;
		if (config != null) {
			return config.cycleTime();
		} else {
			return Cycle.DEFAULT_CYCLE_TIME;
		}
	}

}

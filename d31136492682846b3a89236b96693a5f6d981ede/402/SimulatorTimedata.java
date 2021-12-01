package io.openems.edge.simulator.timedata;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.metatype.annotations.Designate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.NotImplementedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.simulator.CsvUtils;
import io.openems.edge.simulator.DataContainer;
import io.openems.edge.timedata.api.Timedata;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Simulator.Timedata", //
		immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class SimulatorTimedata extends AbstractOpenemsComponent implements Timedata, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		public Doc doc() {
			return this.doc;
		}
	}

	@Reference
	protected ConfigurationAdmin cm;

	private Config config;

	@Activate
	void activate(ComponentContext context, Config config) throws IOException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	public SimulatorTimedata() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Timedata.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Override
	public SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> queryHistoricData(String edgeId,
			ZonedDateTime fromDate, ZonedDateTime toDate, Set<ChannelAddress> channels, int resolution)
			throws OpenemsNamedException {
		try {
			DataContainer data = CsvUtils.readCsvFile(this.getPath(), this.config.format(), 1);
			SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> result = new TreeMap<>();
			ZonedDateTime time = fromDate;
			while (time.isBefore(toDate)) {
				// read Channel values
				SortedMap<ChannelAddress, JsonElement> timeMap = new TreeMap<>();
				for (ChannelAddress channel : channels) {
					timeMap.put(channel, this.getValueAsJson(data, channel));
				}

				// add to result
				result.put(time, timeMap);

				// prepare next time + data
				time = time.plusSeconds(resolution);
				data.nextRecord();
			}
			return result;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			throw new OpenemsException(e.getMessage());
		}
	}

	@Override
	public SortedMap<ChannelAddress, JsonElement> queryHistoricEnergy(String edgeId, ZonedDateTime fromDate,
			ZonedDateTime toDate, Set<ChannelAddress> channels) throws OpenemsNamedException {
		try {
			DataContainer data = CsvUtils.readCsvFile(this.getPath(), this.config.format(), 1);
			SortedMap<ChannelAddress, JsonElement> result = new TreeMap<>();
			for (ChannelAddress channel : channels) {
				result.put(channel, this.getValueAsJson(data, channel));
			}
			return result;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			throw new OpenemsException(e.getMessage());
		}
	}

	@Override
	public SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> queryHistoricEnergyPerPeriod(String edgeId,
			ZonedDateTime fromDate, ZonedDateTime toDate, Set<ChannelAddress> channels, int resolution)
			throws OpenemsNamedException {
		throw new NotImplementedException("QueryHistoryEnergyPerPeriod is not implemented for Simulator");
	}

	/**
	 * Gets the value of the record for the given Channel-Address as Json.
	 * 
	 * @param channel the {@link ChannelAddress}
	 * @return the value as JsonElement
	 */
	private JsonElement getValueAsJson(DataContainer data, ChannelAddress channel) {
		Optional<Float> value = data.getValue(channel.toString());
		if (value.isPresent()) {
			return new JsonPrimitive(value.get());
		} else {
			return JsonNull.INSTANCE;
		}
	}

	/**
	 * Gets the path to the timedata CSV file.
	 * 
	 * @return the absolute path
	 */
	private File getPath() {
		return new File(System.getProperty("user.home"), this.config.filename());
	}

	@Override
	public CompletableFuture<Optional<Object>> getLatestValue(ChannelAddress channelAddress) {
		// TODO implement this method
		return CompletableFuture.completedFuture(Optional.empty());
	}

}

package io.openems.edge.timedata.rrd4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.DsDef;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.RrdRandomAccessFileBackendFactory;
import org.rrd4j.core.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import io.openems.common.OpenemsConstants;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.timedata.CommonTimedataService;
import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.timedata.api.Timedata;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Timedata.Rrd4j", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE)
public class Rrd4jTimedataImpl extends AbstractOpenemsComponent
		implements Rrd4jTimedata, Timedata, OpenemsComponent, EventHandler {

	protected static final String DEFAULT_DATASOURCE_NAME = "value";
	protected static final int DEFAULT_STEP_SECONDS = 300;
	protected static final int DEFAULT_HEARTBEAT_SECONDS = DEFAULT_STEP_SECONDS;

	private static final String RRD4J_PATH = "rrd4j";

	private final Logger log = LoggerFactory.getLogger(Rrd4jTimedataImpl.class);

	private final RecordWorker worker;
	private final RrdRandomAccessFileBackendFactory factory;

	public Rrd4jTimedataImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Timedata.ChannelId.values(), //
				Rrd4jTimedata.ChannelId.values() //
		);
		this.worker = new RecordWorker(this);
		this.factory = new RrdRandomAccessFileBackendFactory();
	}

	@Reference
	protected ComponentManager componentManager;

	protected PersistencePriority persistencePriority = PersistencePriority.MEDIUM;

	@Activate
	void activate(ComponentContext context, Config config) throws Exception {
		this.persistencePriority = config.persistencePriority();
		super.activate(context, config.id(), config.alias(), config.enabled());

		if (config.enabled()) {
			this.worker.activate(config.id());
		}
	}

	@Deactivate
	protected void deactivate() {
		this.worker.deactivate();
		super.deactivate();
	}

	@Override
	public SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> queryHistoricData(String edgeId,
			ZonedDateTime fromDate, ZonedDateTime toDate, Set<ChannelAddress> channels, int resolution)
			throws OpenemsNamedException {
		ZoneId timezone = fromDate.getZone();
		SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> table = new TreeMap<>();

		RrdDb database = null;
		try {
			long fromTimestamp = fromDate.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond();
			long toTimeStamp = toDate.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond();

			for (ChannelAddress channelAddress : channels) {
				Channel<?> channel = this.componentManager.getChannel(channelAddress);
				database = this.getExistingRrdDb(channel.address());
				if (database == null) {
					continue; // not existing -> abort
				}

				ChannelDef chDef = this.getDsDefForChannel(channel.channelDoc().getUnit());
				FetchRequest request = database.createFetchRequest(chDef.consolFun, fromTimestamp, toTimeStamp,
						resolution);

				// Post-Process data
				double[] result = postProcessData(request, resolution);
				database.close();

				for (int i = 0; i < result.length; i++) {
					long timestamp = fromTimestamp + (i * resolution);

					// Prepare result table row
					Instant timestampInstant = Instant.ofEpochSecond(timestamp);
					ZonedDateTime dateTime = ZonedDateTime.ofInstant(timestampInstant, ZoneOffset.UTC)
							.withZoneSameInstant(timezone);
					SortedMap<ChannelAddress, JsonElement> tableRow = table.get(dateTime);
					if (tableRow == null) {
						tableRow = new TreeMap<>();
					}

					double value = result[i];
					if (Double.isNaN(value)) {
						tableRow.put(channelAddress, JsonNull.INSTANCE);
					} else {
						tableRow.put(channelAddress, new JsonPrimitive(value));
					}

					table.put(dateTime, tableRow);
				}
			}

		} catch (Exception e) {
			throw new OpenemsException("Unable to read historic data: " + e.getMessage());
		} finally {
			if (database != null && !database.isClosed()) {
				try {
					database.close();
				} catch (IOException e) {
					this.logWarn(this.log, "Unable to close database: " + e.getMessage());
				}
			}
		}
		return table;
	}

	/**
	 * Post-Process the received data.
	 * 
	 * <p>
	 * This mainly makes sure the data has the correct resolution.
	 * 
	 * @param request    the RRD4j {@link FetchRequest}
	 * @param resolution the resolution in seconds
	 * @return the result array
	 * @throws IOException              on error
	 * @throws IllegalArgumentException on error
	 */
	protected static double[] postProcessData(FetchRequest request, int resolution)
			throws IOException, IllegalArgumentException {
		FetchData data = request.fetchData();
		long step = data.getStep();
		double[] input = data.getValues()[0];

		// Initialize result array
		final double[] result = new double[(int) ((request.getFetchEnd() - request.getFetchStart()) / resolution)];
		for (int i = 0; i < result.length; i++) {
			result[i] = Double.NaN;
		}

		if (step < resolution) {
			// Merge multiple entries to resolution
			if (resolution % step != 0) {
				throw new IllegalArgumentException(
						"Requested resolution [" + resolution + "] is not dividable by RRD4j Step [" + step + "]");
			}
			int merge = (int) (resolution / step);
			double[] buffer = new double[merge];
			for (int i = 1; i < input.length; i += merge) {
				for (int j = 0; j < merge; j++) {
					if (i + j < input.length) {
						buffer[j] = input[i + j];
					} else {
						buffer[j] = Double.NaN;
					}
				}

				// put in result; avoid index rounding error
				int resultIndex = (i - 1) / merge;
				if (resultIndex >= result.length) {
					break;
				}
				result[resultIndex] = TypeUtils.average(buffer);
			}

		} else if (step > resolution) {
			// Split each entry to multiple values
			long resultTimestamp = 0;
			for (int i = 0, inputIndex = 0; i < result.length; i++) {
				inputIndex = Math.min(input.length - 1, (int) (resultTimestamp / step));
				resultTimestamp += resolution;
				result[i] = input[inputIndex];
			}

		} else {
			// Data already matches resolution
			for (int i = 1; i < result.length + 1 && i < input.length; i++) {
				result[i - 1] = input[i];
			}
		}
		return result;
	}

	@Override
	public SortedMap<ChannelAddress, JsonElement> queryHistoricEnergy(String edgeId, ZonedDateTime fromDate,
			ZonedDateTime toDate, Set<ChannelAddress> channels) throws OpenemsNamedException {
		SortedMap<ChannelAddress, JsonElement> table = new TreeMap<>();
		long fromTimestamp = fromDate.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond();
		long toTimeStamp = toDate.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond();

		RrdDb database = null;
		try {
			for (ChannelAddress channelAddress : channels) {
				Channel<?> channel = this.componentManager.getChannel(channelAddress);
				database = this.getExistingRrdDb(channel.address());

				if (database == null) {
					continue; // not existing -> abort
				}

				ChannelDef chDef = this.getDsDefForChannel(channel.channelDoc().getUnit());
				FetchRequest request = database.createFetchRequest(chDef.consolFun, fromTimestamp, toTimeStamp);
				FetchData data = request.fetchData();
				database.close();

				// Find first and last energy value != null
				double first = Double.NaN;
				double last = Double.NaN;
				for (Double tmp : data.getValues(0)) {
					if (Double.isNaN(first) && !Double.isNaN(tmp)) {
						first = tmp;
					}
					if (!Double.isNaN(tmp)) {
						last = tmp;
					}
				}

				// Calculate difference between last and first value
				double value = last - first;

				if (Double.isNaN(value)) {
					table.put(channelAddress, JsonNull.INSTANCE);
				} else {
					table.put(channelAddress, new JsonPrimitive(value));
				}

			}

		} catch (Exception e) {
			throw new OpenemsException("Unable to read historic data: " + e.getMessage());
		} finally {
			if (database != null && !database.isClosed()) {
				try {
					database.close();
				} catch (IOException e) {
					this.logWarn(this.log, "Unable to close database: " + e.getMessage());
				}
			}
		}
		return table;
	}

	@Override
	public SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> queryHistoricEnergyPerPeriod(String edgeId,
			ZonedDateTime fromDate, ZonedDateTime toDate, Set<ChannelAddress> channels, int resolution)
			throws OpenemsNamedException {
		SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> table = new TreeMap<>();

		ZoneId timezone = fromDate.getZone();

		long fromTimestamp = fromDate.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond();
		long toTimeStamp = toDate.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond();

		long nextStamp = fromTimestamp + resolution;
		long timeStamp = fromTimestamp;

		while (nextStamp <= toTimeStamp) {

			Instant timestampInstantFrom = Instant.ofEpochSecond(timeStamp);
			ZonedDateTime dateTimeFrom = ZonedDateTime.ofInstant(timestampInstantFrom, ZoneOffset.UTC)
					.withZoneSameInstant(timezone);

			Instant timestampInstantTo = Instant.ofEpochSecond(nextStamp);
			ZonedDateTime dateTimeTo = ZonedDateTime.ofInstant(timestampInstantTo, ZoneOffset.UTC)
					.withZoneSameInstant(timezone);

			SortedMap<ChannelAddress, JsonElement> tableRow = this.queryHistoricEnergy(null, dateTimeFrom, dateTimeTo,
					channels);

			table.put(dateTimeFrom, tableRow);

			timeStamp = nextStamp;
			nextStamp += resolution;
		}

		return table;

	}

	@Override
	public CompletableFuture<Optional<Object>> getLatestValue(ChannelAddress channelAddress) {
		// Prepare result
		final CompletableFuture<Optional<Object>> result = new CompletableFuture<>();

		CompletableFuture.runAsync(() -> {
			RrdDb database = this.getExistingRrdDb(channelAddress);
			if (database == null) {
				result.complete(Optional.empty());
			}
			try {
				result.complete(Optional.of(database.getLastDatasourceValues()[0]));
			} catch (Exception e) {
				result.complete(Optional.empty());
			} finally {
				try {
					database.close();
				} catch (IOException e) {
					this.logWarn(this.log, "Unable to close Database for [" + channelAddress + "]: " + e.getMessage());
				}
			}
		});

		return result;
	}

	/**
	 * Gets the RRD4j database for the given Channel-Address.
	 * 
	 * <p>
	 * The predefined RRD4J archives match the requirements of
	 * {@link CommonTimedataService#calculateResolution(ZonedDateTime, ZonedDateTime)}
	 * 
	 * @param channelAddress the Channel-Address
	 * @param startTime      the starttime for newly created RrdDbs
	 * @return the RrdDb
	 * @throws IOException        on error
	 * @throws URISyntaxException on error
	 */
	protected synchronized RrdDb getRrdDb(ChannelAddress channelAddress, Unit channelUnit, long startTime)
			throws IOException, URISyntaxException {
		RrdDb rrdDb = this.getExistingRrdDb(channelAddress);
		if (rrdDb != null) {
			// Database exists

			// Update database definition if required
			rrdDb = this.updateRrdDbToLatestDefinition(rrdDb, channelAddress, channelUnit);

			return rrdDb;

		} else {
			// Create new database
			return this.createNewDb(channelAddress, channelUnit, startTime);
		}
	}

	/**
	 * Creates new DB
	 * 
	 * @param channelAddress the {@link ChannelAddress}
	 * @param channelUnit    the {@link Unit} of the Channel
	 * @param startTime      the timestamp of the newly added data
	 * @throws IOException on error
	 */
	private synchronized RrdDb createNewDb(ChannelAddress channelAddress, Unit channelUnit, long startTime)
			throws IOException {
		ChannelDef channelDef = this.getDsDefForChannel(channelUnit);
		RrdDef rrdDef = new RrdDef(//
				this.getDbFile(channelAddress).toURI(), //
				startTime, // Start-Time
				DEFAULT_STEP_SECONDS // Step in [s], default: 300 = 5 minutes
		);
		rrdDef.addDatasource(//
				new DsDef(DEFAULT_DATASOURCE_NAME, //
						channelDef.dsType, //
						DEFAULT_HEARTBEAT_SECONDS, // Heartbeat in [s], default 300 = 5 minutes
						channelDef.minValue, channelDef.maxValue));
		// detailed recordings
		rrdDef.addArchive(channelDef.consolFun, 0.5, 1, 8_928); // 1 step (5 minutes), 8928 rows (31 days)
		rrdDef.addArchive(channelDef.consolFun, 0.5, 12, 8_016); // 12 steps (60 minutes), 8016 rows (334 days)

		return RrdDb.getBuilder() //
				.setBackendFactory(this.factory) //
				.usePool() //
				.setRrdDef(rrdDef) //
				.build();
	}

	/**
	 * Gets an existing RrdDb.
	 * 
	 * @param channelAddress the ChannelAddress
	 * @return the RrdDb or null
	 * @throws IOException        on error
	 * @throws URISyntaxException on error
	 */
	protected synchronized RrdDb getExistingRrdDb(ChannelAddress channelAddress) {
		File file = this.getDbFile(channelAddress);
		if (!file.exists()) {
			return null;
		}
		try {
			return RrdDb.getBuilder() //
					.setBackendFactory(this.factory) //
					.usePool() //
					.setPath(file.toURI()) //
					.build();
		} catch (IOException e) {
			this.logError(this.log, "Unable to open existing RrdDb: " + e.getMessage());
			return null;
		}
	}

	private File getDbFile(ChannelAddress channelAddress) {
		File file = Paths.get(//
				OpenemsConstants.getOpenemsDataDir(), //
				RRD4J_PATH, //
				this.id(), //
				channelAddress.getComponentId(), //
				channelAddress.getChannelId()) //
				.toFile();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	private static class ChannelDef {
		private final DsType dsType;
		private final double minValue;
		private final double maxValue;
		private final ConsolFun consolFun;

		public ChannelDef(DsType dsType, double minValue, double maxValue, ConsolFun consolFun) {
			this.dsType = dsType;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.consolFun = consolFun;
		}
	}

	/**
	 * Defines the datasource properties for a given Channel, i.e. min/max allowed
	 * value and GAUGE vs. COUNTER type.
	 * 
	 * @param channel the Channel
	 * @return the {@link DsDef}
	 */
	private ChannelDef getDsDefForChannel(Unit channelUnit) {
		switch (channelUnit) {
		case AMPERE:
		case AMPERE_HOURS:
		case DEGREE_CELSIUS:
		case DEZIDEGREE_CELSIUS:
		case HERTZ:
		case HOUR:
		case KILOAMPERE_HOURS:
		case KILOOHM:
		case KILOVOLT_AMPERE:
		case KILOVOLT_AMPERE_REACTIVE:
		case KILOWATT:
		case MICROOHM:
		case MILLIAMPERE_HOURS:
		case MILLIAMPERE:
		case MILLIHERTZ:
		case MILLIOHM:
		case MILLISECONDS:
		case MILLIVOLT:
		case MILLIWATT:
		case MINUTE:
		case NONE:
		case WATT:
		case VOLT:
		case VOLT_AMPERE:
		case VOLT_AMPERE_REACTIVE:
		case WATT_HOURS_BY_WATT_PEAK:
		case OHM:
		case SECONDS:
		case THOUSANDTH:
			return new ChannelDef(DsType.GAUGE, Double.NaN, Double.NaN, ConsolFun.AVERAGE);
		case PERCENT:
			return new ChannelDef(DsType.GAUGE, 0, 100, ConsolFun.AVERAGE);
		case ON_OFF:
			return new ChannelDef(DsType.GAUGE, 0, 1, ConsolFun.AVERAGE);
		case CUMULATED_SECONDS:
		case WATT_HOURS:
		case KILOWATT_HOURS:
		case VOLT_AMPERE_HOURS:
		case VOLT_AMPERE_REACTIVE_HOURS:
		case KILOVOLT_AMPERE_REACTIVE_HOURS:
			return new ChannelDef(DsType.GAUGE, Double.NaN, Double.NaN, ConsolFun.MAX);
		}
		throw new IllegalArgumentException("Unhandled Channel unit [" + channelUnit + "]");
	}

	/**
	 * Migrates between different versions of the OpenEMS-RRD4j Definition.
	 * 
	 * @param database       the {@link RrdDb} database
	 * @param channelAddress the {@link ChannelAddress}
	 * @param channelUnit    the {@link Unit} of the Channel
	 * @return new {@link RrdDb}
	 * @throws IOException on error
	 */
	private RrdDb updateRrdDbToLatestDefinition(RrdDb oldDb, ChannelAddress channelAddress, Unit channelUnit)
			throws IOException {
		if (oldDb.getArcCount() > 2 || oldDb.getRrdDef().getStep() == 60) {
			/*
			 * This is an old OpenEMS-RRD4j Definition -> migrate to latest version
			 */
			// Read data of last month
			long lastTimestamp = oldDb.getLastUpdateTime();
			long firstTimestamp = lastTimestamp - (60 /* minute */ * 60 /* hour */ * 24 /* day */ * 31 /* month */);
			FetchRequest fetchRequest = oldDb.createFetchRequest(oldDb.getArchive(0).getConsolFun(), firstTimestamp,
					lastTimestamp);
			FetchData fetchData = fetchRequest.fetchData();
			double[] values = postProcessData(fetchRequest, DEFAULT_HEARTBEAT_SECONDS);
			if (fetchData.getTimestamps().length > 0) {
				firstTimestamp = fetchData.getTimestamps()[0];
			}
			oldDb.close();

			// Delete old file
			Files.delete(Paths.get(oldDb.getCanonicalPath()));

			// Create new database
			RrdDb newDb = this.createNewDb(channelAddress, channelUnit, firstTimestamp - 1);

			// Migrate data
			Sample sample = newDb.createSample();
			for (int i = 0; i < values.length; i++) {
				sample.setTime(firstTimestamp + i * DEFAULT_HEARTBEAT_SECONDS);
				sample.setValue(0, values[i]);
				sample.update();
			}

			this.logInfo(this.log,
					"Migrate RRD4j Database [" + channelAddress.toString() + "] to latest OpenEMS Definition");
			return newDb;

		} else {
			// No Update required
			return oldDb;
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
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
			this.worker.collectData();
			break;
		}
	}

}

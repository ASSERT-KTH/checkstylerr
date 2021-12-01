/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base.reindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexPreservationRepositoryEventPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexPreservationRepositoryEventPlugin.class);
  private boolean clearIndexes = false;
  private boolean optimizeIndexes = true;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN,
        "false", false, false, "Clear all indexes before reindexing them."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES, "Optimize indexes", PluginParameterType.BOOLEAN,
        "true", false, false, "Optimize indexes after reindexing them."));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Rebuild preservation repository event index";
  }

  @Override
  public String getDescription() {
    return "Clears the index and recreates it from actual physical data that exists on the storage. This task aims to fix inconsistencies "
      + "between what is shown in the graphical user interface of the repository and what is actually kept at the storage layer. Such "
      + "inconsistencies may occur for various reasons, e.g. index corruption, ungraceful shutdown of the repository, etc.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES)) {
      clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    }

    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES)) {
      optimizeIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      jobPluginInfo.setSourceObjectsCount(0);
      PluginHelper.updateJobInformation(this, jobPluginInfo);
      pluginReport.setPluginState(PluginState.SUCCESS);

      CloseableIterable<OptionalWithCause<PreservationMetadata>> iterable = model.listPreservationRepositoryEvents();
      int eventCounter = 0;

      for (OptionalWithCause<PreservationMetadata> opm : iterable) {
        if (opm.isPresent()) {
          model.notifyPreservationMetadataCreated(opm.get());
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          pluginReport.setPluginState(PluginState.FAILURE)
            .addPluginDetails("Could not add preservation repository event: " + opm.getCause());
        }
        eventCounter++;
      }
      IOUtils.closeQuietly(iterable);
      jobPluginInfo.setSourceObjectsCount(eventCounter);

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error reindexing RODA entity", e);
      pluginReport.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Could not list preservation repository events");
    }

    return pluginReport;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearRepositoryEventIndex();
      } catch (GenericException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    if (optimizeIndexes) {
      LOGGER.debug("Optimizing indexes");
      try {
        index.optimizeIndex(RodaConstants.INDEX_PRESERVATION_EVENTS);
      } catch (GenericException e) {
        throw new PluginException("Error optimizing index", e);
      }
    }

    return new Report();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new ReindexPreservationRepositoryEventPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Reindex Roda entity";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "All entities were reindexed with success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "An error occured while reindexing all entities";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REINDEX);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

}

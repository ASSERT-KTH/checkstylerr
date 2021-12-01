/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
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

public class FixAncestorsPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FixAncestorsPlugin.class);

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "AIP ancestor hierarchy fix";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Attempts to fix the ancestor hierarchy of the AIPs in the catalogue by removing ghosts (i.e. AIPs with nonexistent ancestors in the catalogue) and merging AIPs with the same Ingest SIP identifier.\nThis task aims to fix problems that may occur when SIPs are ingested but not all the necessary items to construct the catalogue hierarchy have been received or properly ingested.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Void> list)
    throws PluginException {
    try {
      int counter = index.count(IndexedAIP.class,
        new Filter(new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.TRUE.toString()))).intValue();

      // XXX 20160929 Is it really needed? (it is there to be possible to get
      // 100% done reports)
      if (counter == 0) {
        counter = index.count(IndexedAIP.class, Filter.ALL).intValue();
      }

      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, counter);
      jobPluginInfo.setSourceObjectsCount(counter);
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      // FIXME 20161109 hsilva: there should be a parameter to set jobId or
      // empty for all
      PluginHelper.fixParents(this, index, model, Optional.empty());

      jobPluginInfo.incrementObjectsProcessedWithSuccess(counter);
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | JobException
      | NotFoundException e) {
      LOGGER.error("Error while fixing the ancestors.", e);
    }

    return PluginHelper.initPluginReport(this);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new FixAncestorsPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
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
    return "Fixed the ancestor hierarchy";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Fixed the ancestor hierarchy successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Fix of the ancestor hierarchy failed";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MISC);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }
}

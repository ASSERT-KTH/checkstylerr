/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.DummyPlugin;
import org.roda.core.plugins.plugins.PluginThatFailsDuringExecuteMethod;
import org.roda.core.plugins.plugins.PluginThatFailsDuringInit;
import org.roda.core.plugins.plugins.PluginThatFailsDuringXMethod;
import org.roda.core.plugins.plugins.PluginThatStopsItself;
import org.roda.core.plugins.plugins.PluginThatTestsLocking;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class JobsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsTest.class);

  private static Path basePath;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    LOGGER.info("Running Jobs tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testJobExecutingDummyPlugin()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TestsHelper.executeJob(DummyPlugin.class, PluginType.MISC, SelectedItemsNone.create(), JOB_STATE.COMPLETED);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringInit()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      RodaCoreFactory.getPluginManager().registerPlugin(new PluginThatFailsDuringInit());
      Assert.fail("Plugin should not load & therefore an exception was expected!");
    } catch (PluginException e) {
      // do nothing as it is expected
    }
    Job job = TestsHelper.executeJob(PluginThatFailsDuringInit.class, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.FAILED_TO_COMPLETE);
    Assert.assertEquals(job.getStateDetails(), "Plugin is NULL");
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringBeforeAllExecute()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatFailsDuringXMethod.BEFORE_ALL_EXECUTE, "");
    TestsHelper.executeJob(PluginThatFailsDuringXMethod.class, parameters, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.FAILED_TO_COMPLETE);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringExecute()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatFailsDuringXMethod.ON_EXECUTE, "");
    TestsHelper.executeJob(PluginThatFailsDuringXMethod.class, parameters, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.FAILED_TO_COMPLETE);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringExecuteInParallel() throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    JobsHelper.setBlockSize(1);

    ModelService modelService = RodaCoreFactory.getModelService();
    String aip1 = modelService.createAIP(null, "misc", new Permissions(), RodaConstants.ADMIN).getId();
    String aip2 = modelService.createAIP(null, "misc", new Permissions(), RodaConstants.ADMIN).getId();

    Map<String, String> parameters = new HashMap<>();
    Job job;
    JobStats jobStats;

    // 1) don't fail at all
    parameters.put(aip1, "false");
    parameters.put(aip2, "false");
    job = TestsHelper.executeJob(PluginThatFailsDuringExecuteMethod.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip1, aip2), JOB_STATE.COMPLETED);
    Assert.assertNotNull(job.getStateDetails());
    Assert.assertEquals(job.getStateDetails(), "");
    jobStats = job.getJobStats();
    assertJobStats(jobStats, 100, 0, 2, 0, 2, 0);

    // 2) fail in one (order doesn't matter as orchestration is asynchronous)
    parameters.put(aip1, "false");
    parameters.put(aip2, "true");
    job = TestsHelper.executeJob(PluginThatFailsDuringExecuteMethod.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip1, aip2), JOB_STATE.FAILED_TO_COMPLETE);
    Assert.assertNotNull(job.getStateDetails());
    Assert.assertTrue(StringUtils.containsIgnoreCase(job.getStateDetails(), "exception"));
    jobStats = job.getJobStats();
    assertJobStats(jobStats, 100, 0, 2, 1, 1, 0);

    // 3) fail in both
    parameters.put(aip1, "true");
    parameters.put(aip2, "true");
    job = TestsHelper.executeJob(PluginThatFailsDuringExecuteMethod.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip1, aip2), JOB_STATE.FAILED_TO_COMPLETE);
    Assert.assertNotNull(job.getStateDetails());
    Assert.assertTrue(StringUtils.containsIgnoreCase(job.getStateDetails(), "exception"));
    jobStats = job.getJobStats();
    assertJobStats(jobStats, 100, 0, 2, 2, 0, 0);
  }

  private void assertJobStats(JobStats jobStats, int expectedCompletionPercentage,
    int expectedSourceObjectsBeingProcessed, int expectedSourceObjectsCount,
    int expectedSourceObjectsProcessedWithFailure, int expectedSourceObjectsProcessedWithSuccess,
    int expectedSourceObjectsWaitingToBeProcessed) {

    Assert.assertEquals(jobStats.getSourceObjectsCount(), expectedSourceObjectsCount);
    Assert.assertEquals(jobStats.getSourceObjectsProcessedWithSuccess(), expectedSourceObjectsProcessedWithSuccess);
    Assert.assertEquals(jobStats.getSourceObjectsProcessedWithFailure(), expectedSourceObjectsProcessedWithFailure);
    Assert.assertEquals(jobStats.getSourceObjectsBeingProcessed(), expectedSourceObjectsBeingProcessed);
    Assert.assertEquals(jobStats.getSourceObjectsWaitingToBeProcessed(), expectedSourceObjectsWaitingToBeProcessed);
    Assert.assertEquals(jobStats.getCompletionPercentage(), expectedCompletionPercentage);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringAfterAllExecute()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatFailsDuringXMethod.AFTER_ALL_EXECUTE, "");
    TestsHelper.executeJob(PluginThatFailsDuringXMethod.class, parameters, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.FAILED_TO_COMPLETE);
  }

  @Test
  public void testJobExecutingPluginThatStopsItselfUsingOrchestratorStop()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    ModelService modelService = RodaCoreFactory.getModelService();
    List<String> aips = new ArrayList<>();
    try {
      for (int i = 0; i < 30; i++) {
        aips.add(modelService
          .createAIP(null, RodaConstants.REPRESENTATION_TYPE_MIXED, new Permissions(), RodaConstants.ADMIN).getId());
      }
    } catch (AlreadyExistsException e) {
      // do nothing
    }

    int originalNumberOfJobWorkers = JobsHelper.getNumberOfJobsWorkers();
    int originalBlockSize = JobsHelper.getBlockSize();

    // setting new/test value for the number of job workers & block size
    JobsHelper.setNumberOfJobsWorkers(1);
    JobsHelper.setBlockSize(1);

    TestsHelper.executeJob(PluginThatStopsItself.class, PluginType.MISC, SelectedItemsList.create(AIP.class, aips),
      JOB_STATE.STOPPED);

    // resetting number of job workers & block size
    JobsHelper.setNumberOfJobsWorkers(originalNumberOfJobWorkers);
    JobsHelper.setBlockSize(originalBlockSize);
  }

  /**
   * 20160914 hsilva: this method tests orchestration to ensure that, even if
   * there are no objects to pass to the plugin, the job comes to an end (i.e.
   * complete state)
   */
  @Test
  public void testJobRunningInContainerWithNoObjectsInIt()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    int originalSyncTimeout = JobsHelper.getSyncTimeout();
    // set sync timeout as the method execute() will do nothing as well as
    // receive nothing. and if some problem exists with job state transitions 5
    // seconds will be enough as a timeout will be thrown
    JobsHelper.setSyncTimeout(5);
    TestsHelper.executeJob(DummyPlugin.class, PluginType.MISC, SelectedItemsNone.create(), JOB_STATE.COMPLETED);

    JobsHelper.setSyncTimeout(originalSyncTimeout);
  }

  @Test
  public void testPluginProcessingTheSameObjectInSeveralThreadsWithAutoLocking() throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    // set block size to 1 in order to have several "threads" when using list of
    // objects of size 2 or greater
    JobsHelper.setBlockSize(1);

    ModelService modelService = RodaCoreFactory.getModelService();
    IndexService indexService = RodaCoreFactory.getIndexService();
    AIP aip = modelService.createAIP(null, RodaConstants.REPRESENTATION_TYPE_MIXED, new Permissions(),
      RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatTestsLocking.PLUGIN_PARAM_AUTO_LOCKING, "true");
    // execute plugin via list in the same object (two ids, the same object)
    Job job = TestsHelper.executeJob(PluginThatTestsLocking.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip.getId(), aip.getId()), JOB_STATE.COMPLETED);

    // asserts
    List<Report> jobReports = TestsHelper.getJobReports(indexService, job);
    Assert.assertEquals(jobReports.size(), 1);
    String pluginDetails = jobReports.get(0).getPluginDetails();
    String[] pluginDetailsSplitted = pluginDetails.split(System.lineSeparator());
    Assert.assertEquals(pluginDetailsSplitted.length, 5);
    Assert.assertTrue(pluginDetails.contains(PluginThatTestsLocking.PLUGIN_DETAILS_AT_LEAST_ONE_LOCK_REQUEST_WAITING));
    int dates = 0;
    String year = Calendar.getInstance().get(Calendar.YEAR) + "";
    for (String details : pluginDetailsSplitted) {
      if (details.contains(year)) {
        dates++;
      }
    }
    // details are not overwritten
    Assert.assertEquals(dates, 4);
  }

  @Test
  public void testPluginProcessingTheSameObjectInSeveralThreadsWithoutAutoLocking() throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    // set block size to 1 in order to have several "threads" when using list of
    // objects of size 2 or greater
    JobsHelper.setBlockSize(1);

    ModelService modelService = RodaCoreFactory.getModelService();
    IndexService indexService = RodaCoreFactory.getIndexService();
    AIP aip = modelService.createAIP(null, RodaConstants.REPRESENTATION_TYPE_MIXED, new Permissions(),
      RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatTestsLocking.PLUGIN_PARAM_AUTO_LOCKING, "false");
    // execute plugin via list in the same object (two ids, the same object)
    Job job = TestsHelper.executeJob(PluginThatTestsLocking.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip.getId(), aip.getId()), JOB_STATE.COMPLETED);

    // asserts
    List<Report> jobReports = TestsHelper.getJobReports(indexService, job);
    Assert.assertEquals(jobReports.size(), 1);
    String pluginDetails = jobReports.get(0).getPluginDetails();
    String[] pluginDetailsSplitted = pluginDetails.split(System.lineSeparator());
    Assert.assertEquals(pluginDetailsSplitted.length, 2);
    Assert.assertFalse(pluginDetails.contains(PluginThatTestsLocking.PLUGIN_DETAILS_AT_LEAST_ONE_LOCK_REQUEST_WAITING));
    int dates = 0;
    String year = Calendar.getInstance().get(Calendar.YEAR) + "";
    for (String details : pluginDetailsSplitted) {
      if (details.contains(year)) {
        dates++;
      }
    }
    // details are overwritten
    Assert.assertEquals(dates, 2);
  }

}

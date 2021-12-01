/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.EARKSIPToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.MinimalIngestPlugin;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class PluginReportContentTest {

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static Path corporaPath;

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginReportContentTest.class);

  private static final String CORPORA_PDF = "test.docx";
  private static final String CORPORA_TEST1 = "test1";
  private static final String CORPORA_TEST1_TXT = "test1.txt";
  private static final int GENERATED_FILE_SIZE = 100;
  private static final String AIP_CREATOR = "admin";

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(PluginReportContentTest.class, true,
      PosixFilePermissions
        .asFileAttribute(new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE))));

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = EARKSIPPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    LOGGER.debug("Running index tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {

    // delete all AIPs
    index.execute(IndexedAIP.class, Filter.ALL, new ArrayList<>(), new IndexRunnable<IndexedAIP>() {
      @Override
      public void run(IndexedAIP item) throws GenericException, RequestNotValidException, AuthorizationDeniedException {
        try {
          model.deleteAIP(item.getId());
        } catch (NotFoundException e) {
          // do nothing
        }
      }
    }, e -> Assert.fail("Error cleaning up", e));

    // delete all Transferred Resources
    index.execute(TransferredResource.class, Filter.ALL, new ArrayList<>(), new IndexRunnable<TransferredResource>() {

      @Override
      public void run(TransferredResource item) throws GenericException, AuthorizationDeniedException {
        model.deleteTransferredResource(item);
      }
    }, e -> Assert.fail("Error cleaning up", e));
  }

  private ByteArrayInputStream generateContentData() {
    return new ByteArrayInputStream(RandomStringUtils.randomAscii(GENERATED_FILE_SIZE).getBytes());
  }

  private TransferredResource createCorpora() throws NotFoundException, GenericException, RequestNotValidException,
    AlreadyExistsException, AuthorizationDeniedException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    String parentUUID = f.createFolder(null, "test").getUUID();
    String test1UUID = f.createFolder(parentUUID, CORPORA_TEST1).getUUID();
    String test2UUID = f.createFolder(parentUUID, "test2").getUUID();
    String test3UUID = f.createFolder(parentUUID, "test3").getUUID();

    f.createFile(parentUUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(parentUUID, "test2.txt", generateContentData());
    f.createFile(parentUUID, "test3.txt", generateContentData());
    f.createFile(test1UUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(test1UUID, "test2.txt", generateContentData());
    f.createFile(test1UUID, "test3.txt", generateContentData());
    f.createFile(test2UUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(test2UUID, "test2.txt", generateContentData());
    f.createFile(test2UUID, "test3.txt", generateContentData());
    f.createFile(test3UUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(test3UUID, "test2.txt", generateContentData());
    f.createFile(test3UUID, "test3.txt", generateContentData());

    f.createFile(parentUUID, CORPORA_PDF, getClass().getResourceAsStream("/corpora/Media/" + CORPORA_PDF));

    index.commit(TransferredResource.class);

    return index.retrieve(TransferredResource.class, IdUtils.createUUID("test"), new ArrayList<>());
  }

  @Test
  public void ingestCorporaTest() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {
    AIP root = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), AIP_CREATOR);

    TransferredResource transferredResource = createCorpora();
    Assert.assertEquals(transferredResource == null, false);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, TransferredResourceToAIPPlugin.class.getName());

    Job job = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    List<Report> jobReports = TestsHelper.getJobReports(index, job, true);
    AssertJUnit.assertEquals(1, jobReports.size());

    for (Report report : jobReports) {
      Assert.assertEquals(report.getReports().size(), MinimalIngestPlugin.TOTAL_STEPS);
      Report first = report.getReports().get(0);
      Report last = report.getReports().get(MinimalIngestPlugin.TOTAL_STEPS - 1);
      Assert.assertEquals(report.getDateCreated().equals(report.getDateUpdated()), false);
      Assert.assertEquals(report.getDateCreated().equals(first.getDateCreated()), true);
      Assert.assertEquals(report.getDateUpdated().equals(last.getDateUpdated()), true);
    }
  }

  @Test
  public void siegfriedCorporaTestAIP() throws RODAException {
    AIP root = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), AIP_CREATOR);

    TransferredResource transferredResource = createCorpora();
    Assert.assertEquals(transferredResource == null, false);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, TransferredResourceToAIPPlugin.class.getName());

    Job ingest = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    List<Report> ingestReports = TestsHelper.getJobReports(index, ingest, true);
    Assert.assertEquals(ingestReports.size(), 1);
    String aipId = ingestReports.get(0).getOutcomeObjectId();

    Job job = TestsHelper.executeJob(SiegfriedPlugin.class, new HashMap<>(), PluginType.MISC,
      SelectedItemsList.create(AIP.class, aipId));

    List<Report> jobReports = TestsHelper.getJobReports(index, job, true);
    Assert.assertEquals(jobReports.size(), 1);

    Report report = jobReports.get(0);
    AssertJUnit.assertEquals(1, report.getReports().size());
    Report innerReport = report.getReports().get(0);
    AssertJUnit.assertEquals(false, report.getDateCreated().equals(report.getDateUpdated()));
    AssertJUnit.assertEquals(true, report.getDateCreated().equals(innerReport.getDateCreated()));
    AssertJUnit.assertEquals(true, report.getDateUpdated().equals(innerReport.getDateUpdated()));
  }

  @Test
  public void siegfriedCorporaTestRepresentation() throws RODAException {
    AIP root = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), AIP_CREATOR);

    TransferredResource transferredResource = createCorpora();
    AssertJUnit.assertEquals(transferredResource == null, false);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, TransferredResourceToAIPPlugin.class.getName());

    Job ingest = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    List<Report> ingestReports = TestsHelper.getJobReports(index, ingest, true);
    AssertJUnit.assertEquals(1, ingestReports.size());

    IndexResult<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, Filter.ALL, Sorter.NONE,
      new Sublist(0, 1), new ArrayList<>());
    String representationUUID = reps.getResults().get(0).getUUID();

    Job job = TestsHelper.executeJob(SiegfriedPlugin.class, new HashMap<>(), PluginType.MISC,
      SelectedItemsList.create(IndexedRepresentation.class, representationUUID));

    List<Report> jobReports = TestsHelper.getJobReports(index, job, true);
    AssertJUnit.assertEquals(1, jobReports.size());

    Report report = jobReports.get(0);
    AssertJUnit.assertEquals(1, report.getReports().size());
    Report innerReport = report.getReports().get(0);
    AssertJUnit.assertEquals(false, report.getDateCreated().equals(report.getDateUpdated()));
    AssertJUnit.assertEquals(true, report.getDateCreated().equals(innerReport.getDateCreated()));
    AssertJUnit.assertEquals(true, report.getDateUpdated().equals(innerReport.getDateUpdated()));
  }

  @Test
  public void siegfriedCorporaTestFile() throws RODAException {
    AIP root = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), AIP_CREATOR);

    TransferredResource transferredResource = createCorpora();
    AssertJUnit.assertEquals(transferredResource == null, false);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, TransferredResourceToAIPPlugin.class.getName());

    Job ingest = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    List<Report> ingestReports = TestsHelper.getJobReports(index, ingest, true);
    AssertJUnit.assertEquals(1, ingestReports.size());

    IndexResult<IndexedFile> files = index.find(IndexedFile.class, Filter.ALL, Sorter.NONE, new Sublist(0, 1),
      new ArrayList<>());
    String fileUUID = files.getResults().get(0).getUUID();

    Job job = TestsHelper.executeJob(SiegfriedPlugin.class, new HashMap<>(), PluginType.MISC,
      SelectedItemsList.create(IndexedFile.class, fileUUID));

    List<Report> jobReports = TestsHelper.getJobReports(index, job, true);
    AssertJUnit.assertEquals(1, jobReports.size());

    Report report = jobReports.get(0);
    AssertJUnit.assertEquals(1, report.getReports().size());
    Report innerReport = report.getReports().get(0);
    AssertJUnit.assertEquals(false, report.getDateCreated().equals(report.getDateUpdated()));
    AssertJUnit.assertEquals(true, report.getDateCreated().equals(innerReport.getDateCreated()));
    AssertJUnit.assertEquals(true, report.getDateUpdated().equals(innerReport.getDateUpdated()));
  }

  @Test
  public void testIngestReportsInNto1Scenario() throws RODAException, URISyntaxException, IOException {
    // create & ingest SIP
    TransferredResource transferredResource = EARKSIPPluginsTest.createIngestCorpora(corporaPath, index);
    AssertJUnit.assertNotNull("Transferred resource cannot be null", transferredResource);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, EARKSIPToAIPPlugin.class.getName());

    Job ingest = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    List<Report> ingestReports = TestsHelper.getJobReports(index, ingest, true);
    AssertJUnit.assertEquals(1, ingestReports.size());

    // create & ingest 2 SIP updates
    transferredResource = EARKSIPPluginsTest.createIngestUpdateCorpora(corporaPath, index, null);
    AssertJUnit.assertNotNull("Transferred resource cannot be null", transferredResource);
    TransferredResource transferredResource2 = EARKSIPPluginsTest.createIngestUpdateCorpora(corporaPath, index,
      CorporaConstants.EARK_SIP_UPDATE.replaceFirst("\\.zip", "2.zip"));
    AssertJUnit.assertNotNull("Transferred resource cannot be null", transferredResource2);

    parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, EARKSIPToAIPPlugin.class.getName());
    ingest = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP, SelectedItemsList
      .create(TransferredResource.class, transferredResource.getUUID(), transferredResource2.getUUID()));

    // ensure that reports & inner reports are well formed (#, ids, etc)
    ingestReports = TestsHelper.getJobReports(index, ingest, true);
    AssertJUnit.assertEquals(2, ingestReports.size());
    for (Report report : ingestReports) {
      AssertJUnit.assertEquals(MinimalIngestPlugin.TOTAL_STEPS, report.getReports().size());

      String jobId = report.getJobId();
      String sourceObjectId = report.getSourceObjectId();
      String outcomeObjectId = report.getOutcomeObjectId();
      for (Report innerReport : report.getReports()) {
        AssertJUnit.assertEquals(IdUtils.getJobReportId(jobId, sourceObjectId, outcomeObjectId), innerReport.getId());
      }
    }
  }
}

package eu.dzhw.fdz.metadatamanagement.datasetmanagement.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.dzhw.fdz.metadatamanagement.common.domain.Task;
import eu.dzhw.fdz.metadatamanagement.common.domain.projections.IdAndVersionProjection;
import eu.dzhw.fdz.metadatamanagement.common.rest.util.ZipUtil;
import eu.dzhw.fdz.metadatamanagement.common.service.TaskService;
import eu.dzhw.fdz.metadatamanagement.datasetmanagement.domain.DataSet;
import eu.dzhw.fdz.metadatamanagement.datasetmanagement.exception.TemplateIncompleteException;
import eu.dzhw.fdz.metadatamanagement.datasetmanagement.repository.DataSetRepository;
import eu.dzhw.fdz.metadatamanagement.filemanagement.service.FileService;
import eu.dzhw.fdz.metadatamanagement.instrumentmanagement.domain.Instrument;
import eu.dzhw.fdz.metadatamanagement.instrumentmanagement.repository.InstrumentRepository;
import eu.dzhw.fdz.metadatamanagement.projectmanagement.domain.Release;
import eu.dzhw.fdz.metadatamanagement.projectmanagement.service.DataAcquisitionProjectVersionsService;
import eu.dzhw.fdz.metadatamanagement.questionmanagement.domain.Question;
import eu.dzhw.fdz.metadatamanagement.questionmanagement.repository.QuestionRepository;
import eu.dzhw.fdz.metadatamanagement.studymanagement.domain.Study;
import eu.dzhw.fdz.metadatamanagement.studymanagement.repository.StudyRepository;
import eu.dzhw.fdz.metadatamanagement.variablemanagement.domain.RelatedQuestion;
import eu.dzhw.fdz.metadatamanagement.variablemanagement.domain.ValidResponse;
import eu.dzhw.fdz.metadatamanagement.variablemanagement.domain.Variable;
import eu.dzhw.fdz.metadatamanagement.variablemanagement.repository.VariableRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * This service fill tex templates with data and put it into the gridfs / mongodb.
 *
 * @author Daniel Katzberg
 */
@Service
@Slf4j
public class DataSetReportService {

  @Autowired
  private FileService fileService;

  @Autowired
  private DataSetRepository dataSetRepository;

  @Autowired
  private VariableRepository variableRepository;

  @Autowired
  private QuestionRepository questionRepository;

  @Autowired
  private StudyRepository studyRepository;

  @Autowired
  private InstrumentRepository instrumentRepository;

  @Autowired
  private DataAcquisitionProjectVersionsService projectVersionsService;

  @Autowired
  private TaskService taskService;

  /**
   * The Escape Prefix handles the escaping of special latex signs within data information. This
   * Prefix will be copied before the template source code.
   */
  private static final String ESCAPE_PREFIX =
      "<#escape x as x?replace(\"\\\\\", \"\\\\textbackslash{}\")"
          + "?replace(\"{\", \"\\\\{\")?replace(\"}\", \"\\\\}\")"
          + "?replace(\"#\", \"\\\\#\")?replace(\"$\", \"\\\\$\")"
          + "?replace(\"%\", \"\\\\%\")?replace(\"&\", \"\\\\&\")"
          + "?replace(\"^\", \"\\\\textasciicircum{}\")?replace(\"_\", \"\\\\_\")"
          + "?replace(\">\", \"\\\\textgreater{}\")?replace(\"<\", \"\\\\textless{}\")"
          + "?replace(\"~\", \"\\\\textasciitilde{}\")" + "?replace(\"\\r\\n\", \"\\\\par  \")"
          + "?replace(\"\\n\", \"\\\\par  \")>";

  /**
   * The Escape Suffix closes the escaping prefix. This Prefix will be copied after the template
   * source code.
   */
  private static final String ESCAPE_SUFFIX = "</#escape>";


  /**
   * Zip Mime Content Type.
   */
  private static final String CONTENT_TYPE_ZIP = "application/zip";

  /**
   * Files which will be filled by the freemarker code.
   */
  private static final String KEY_VARIABLELIST = "Variablelist.tex";
  private static final String KEY_MAIN = "Main.tex";
  private static final String KEY_VARIABLE = "variables/Variable.tex";

  /**
   * This service method will receive a tex template as a string and an id of a data set. With this
   * id, the service will load the data set for receiving all depending information, which are
   * needed for filling of the tex template with data.
   *
   * @param multiPartFile The uploaded zip file
   * @param dataSetId An id of the data set.
   * @param task the task to update the status of the pro
   * @return The name of the saved tex template in the GridFS / MongoDB.
   * @throws TemplateException Handles templates exceptions.
   * @throws IOException Handles IO Exception for the template.
   */
  @Async
  public Future<String> generateReport(MultipartFile multiPartFile, String dataSetId, Task task)
      throws IOException {

    // Configuration, based on Freemarker Version 2.3.23
    Configuration templateConfiguration = new Configuration(Configuration.VERSION_2_3_23);
    templateConfiguration.setDefaultEncoding(StandardCharsets.UTF_8.toString());
    templateConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    templateConfiguration.setNumberFormat("0.######");

    // Prepare Zip enviroment config
    Map<String, String> env = new HashMap<>();
    env.put("create", "true");
    env.put("encoding", StandardCharsets.UTF_8.name());

    // Create tmp file
    Path zipTmpFilePath = Files.createTempFile(dataSetId.replace("!", ""), ".zip");
    File zipTmpFile = zipTmpFilePath.toFile();
    multiPartFile.transferTo(zipTmpFile);
    zipTmpFile.setWritable(true);
    URI uriOfZipFile = URI.create("jar:" + zipTmpFilePath.toUri());
    try (FileSystem zipFileSystem = FileSystems.newFileSystem(uriOfZipFile, env);) {
      // Check missing files.
      List<String> missingTexFiles = this.validateDataSetReportStructure(zipFileSystem);
      if (!missingTexFiles.isEmpty()) {
        String message = "data-set-management.error" + ".files-in-template-zip-incomplete";
        TemplateIncompleteException incompleteException =
            new TemplateIncompleteException(message, missingTexFiles);
        log.warn(message + missingTexFiles);
        taskService.handleErrorTask(task, incompleteException);
        return new AsyncResult<String>(null);
      }
      // Read the three files with freemarker code
      Path pathToMainTexFile = zipFileSystem.getPath(KEY_MAIN);
      String texMainFileStr = ZipUtil.readFileFromZip(pathToMainTexFile);
      Path pathToVariableListTexFile = zipFileSystem.getPath(KEY_VARIABLELIST);
      String texVariableListFileStr = ZipUtil.readFileFromZip(pathToVariableListTexFile);
      Path pathToVariableTexFile = zipFileSystem.getPath(KEY_VARIABLE);
      String texVariableFileStr = ZipUtil.readFileFromZip(pathToVariableTexFile);

      // Load data for template only once
      Map<String, Object> dataForTemplate = this.loadDataForTemplateFilling(dataSetId);
      try {
        String variableListFilledStr = this.fillTemplate(texVariableListFileStr,
            templateConfiguration, dataForTemplate, KEY_VARIABLELIST);
        ZipUtil.writeFileToZip(pathToVariableListTexFile, variableListFilledStr);
        String mainFilledStr =
            this.fillTemplate(texMainFileStr, templateConfiguration, dataForTemplate, KEY_MAIN);
        ZipUtil.writeFileToZip(pathToMainTexFile, mainFilledStr);
      } catch (TemplateException e) {
        taskService.handleErrorTask(task, e);
        return new AsyncResult<String>(null);
      }
      // Create Variables pages
      @SuppressWarnings("unchecked")
      Map<String, Variable> variablesMap = (Map<String, Variable>) dataForTemplate.get("variables");
      Collection<Variable> variables = variablesMap.values();

      for (Variable variable : variables) {
        // filledTemplates.put("variables/" + variable.getName() + ".tex",
        try {
          dataForTemplate.put("variable", variable);
          String filledVariablesFile = fillTemplate(texVariableFileStr, templateConfiguration,
              dataForTemplate, KEY_VARIABLE);
          Path pathOfVariable = Paths.get("variables/" + variable.getName() + ".tex");
          final Path root = zipFileSystem.getPath("/");
          final Path dest = zipFileSystem.getPath(root.toString(), pathOfVariable.toString());
          ZipUtil.writeFileToZip(dest, filledVariablesFile);
        } catch (TemplateException te) {
          log.warn("templage invalid", te);
          taskService.handleErrorTask(task, te);
          return new AsyncResult<String>(null);
        }
      }

      // Delete Variables.tex file from zip
      Files.delete(pathToVariableTexFile);
    }

    // Save into MongoDB / GridFS
    String fileName = this.saveCompleteZipFile(zipTmpFile, multiPartFile.getOriginalFilename());
    taskService.handleTaskDone(task, URI.create(fileName));
    return new AsyncResult<>(fileName);
  }



  /**
   * Checks for all files which are included for the tex template.
   * 
   * @param zipFileSystem The zip file as file system
   * @return True if all files are included. False min one file is missing.
   */
  private List<String> validateDataSetReportStructure(FileSystem zipFileSystem) {
    List<String> missingTexFiles = new ArrayList<>();

    // NO Check for References.bib. This file is just optional has has to be added manually.

    Path mainFile = zipFileSystem.getPath(zipFileSystem.getPath("/").toString(), KEY_MAIN);
    if (!Files.exists(mainFile)) {
      missingTexFiles.add(KEY_MAIN);
    }

    Path variableFile = zipFileSystem.getPath(zipFileSystem.getPath("/").toString(), KEY_VARIABLE);
    if (!Files.exists(variableFile)) {
      missingTexFiles.add(KEY_VARIABLE);
    }

    Path variableListFile =
        zipFileSystem.getPath(zipFileSystem.getPath("/").toString(), KEY_VARIABLELIST);
    if (!Files.exists(variableListFile)) {
      missingTexFiles.add(KEY_VARIABLELIST);
    }

    return missingTexFiles;
  }



  /**
   * This method fills the tex templates.
   *
   * @param templateContent The content of a tex template.
   * @param templateConfiguration The configuration for freemarker.
   * @param fileName filename of the script which will be filled in this method.
   * @return The filled tex templates as byte array.
   * @throws IOException Handles IO Exception.
   * @throws TemplateException Handles template Exceptions.
   */
  private String fillTemplate(String templateContent, Configuration templateConfiguration,
      Map<String, Object> dataForTemplate, String fileName) throws IOException, TemplateException {

    String templateName = "texTemplate";
    if (fileName != null && fileName.trim().length() > 0) {
      templateName = fileName;
    }

    // Read Template and escape elements
    Template texTemplate = new Template(templateName,
        (ESCAPE_PREFIX + templateContent + ESCAPE_SUFFIX), templateConfiguration);

    try (Writer stringWriter = new StringWriter()) {
      texTemplate.process(dataForTemplate, stringWriter);

      stringWriter.flush();
      return stringWriter.toString();
    }
  }

  /**
   * This method save a latex file into GridFS/MongoDB based on a byteArrayOutputStream.
   *
   * @param fileName The name of the file to be saved
   * @return return the file name of the saved latex template in the GridFS / MongoDB.
   * @throws IOException thrown if a stream cannot be closed
   */
  @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
  private String saveCompleteZipFile(File zipFile, String fileName) throws IOException {
    // No Update by API, so we have to delete first.
    fileService.deleteTempFile(fileName);

    // Save tex file
    return fileService.saveTempFile(new FileInputStream(zipFile), fileName, CONTENT_TYPE_ZIP);
  }

  /**
   * This method load all needed objects from the db for filling the tex template.
   *
   * @param dataSetId An id of the data acquision project id.
   * @return A HashMap with all data for the template filling. The Key is the name of the Object,
   *         which is used in the template.
   */
  private Map<String, Object> loadDataForTemplateFilling(String dataSetId) {

    // Create Map for the template
    Map<String, Object> dataForTemplate = new HashMap<>();

    // Create Information for the latex template.
    dataForTemplate = this.addStudyAndDataSetAndLastRelease(dataForTemplate, dataSetId);
    dataForTemplate = this.createVariableDependingMaps(dataForTemplate);

    return dataForTemplate;
  }

  /**
   * This is a "fluent" method for the Map with created Objects of the latex template. Added data
   * set information to the map of objects for the template.
   *
   * @param dataForTemplate The map for the template with all added objects before this method.
   * @param dataSetId The id of the used data set; Root Element of the report.
   * @return The map for the template as fluent result. Added some created elements within this
   *         method.
   */
  private Map<String, Object> addStudyAndDataSetAndLastRelease(Map<String, Object> dataForTemplate,
      String dataSetId) {
    // Get DataSet and check the valid result
    DataSet dataSet = this.dataSetRepository.findById(dataSetId).get();
    Study study = this.studyRepository.findById(dataSet.getStudyId()).get();
    Release lastRelease =
        projectVersionsService.findLastRelease(dataSet.getDataAcquisitionProjectId());

    dataForTemplate.put("study", study);
    dataForTemplate.put("dataSet", dataSet);
    dataForTemplate.put("lastRelease", lastRelease);

    return dataForTemplate;
  }

  /**
   * Fluent Method for creating Map Objects for the latex template. Creates the follow Maps:
   * questions : Map with all question, which are connected by variables of a given data set (key is
   * variable.questionId) isAMissingCounterMap: A map with counter, how many isAMissing Values a
   * variable has (key is variable.id) firstTenValues: The first ten isAMissing values for one tex
   * template layout. (key is variable.id) lastTenValues: The last ten isAMissing values for one tex
   * template layout. (key is variable.id)
   * 
   * @param dataForTemplate The map for the template with all added objects before this method.
   * @return The map for the template as fluent result. Added some created elements within this
   *         method.
   */
  private Map<String, Object> createVariableDependingMaps(Map<String, Object> dataForTemplate) {

    // Create a Map of Variables
    String dataSetId = ((DataSet) dataForTemplate.get("dataSet")).getId();
    List<Variable> variables =
        this.variableRepository.findByDataSetIdOrderByIndexInDataSetAsc(dataSetId);
    Map<String, Variable> variablesMap = Maps.uniqueIndex(variables, new VariableFunction());
    dataForTemplate.put("variables", variablesMap);

    // Create different information from the variable
    Map<String, Question> questionsMap = new HashMap<>();
    Map<String, Instrument> instrumentMap = new HashMap<>();
    Map<String, List<ValidResponse>> firstTenValidResponses = new HashMap<>();
    Map<String, List<ValidResponse>> lastTenValidResponses = new HashMap<>();
    Map<String, List<IdAndVersionProjection>> sameVariablesInPanel = new HashMap<>();


    for (Variable variable : variables) {
      int sizeValidResponses = 0;
      if (variable.getDistribution() != null
          && variable.getDistribution().getValidResponses() != null) {
        sizeValidResponses = variable.getDistribution().getValidResponses().size();
      }

      // Create a Map with Questions
      if (variable.getRelatedQuestions() != null && !variable.getRelatedQuestions().isEmpty()) {
        for (RelatedQuestion relatedQuestion : variable.getRelatedQuestions()) {
          // question is unknown. add it to the question map.
          if (!questionsMap.containsKey(relatedQuestion.getQuestionId())) {
            this.questionRepository.findById(relatedQuestion.getQuestionId())
                .ifPresent(question -> questionsMap.put(relatedQuestion.getQuestionId(), question));
          }
        }
      }

      // Create a Map with Instruments
      if (!questionsMap.isEmpty()) {
        questionsMap.values().forEach(question -> {
          if (!instrumentMap.containsKey(question.getInstrumentId())) {
            this.instrumentRepository.findById(question.getInstrumentId())
                .ifPresent(instrument -> instrumentMap.put(question.getInstrumentId(), instrument));
          }
        });
      }

      // Create the first and last ten isAMissing Values to different list, if there are more
      // than 20.
      if (sizeValidResponses > 20) {
        firstTenValidResponses.put(variable.getId(),
            variable.getDistribution().getValidResponses().subList(0, 10));
        lastTenValidResponses.put(variable.getId(), variable.getDistribution().getValidResponses()
            .subList(sizeValidResponses - 10, sizeValidResponses));
      }

      if (variable.getPanelIdentifier() != null) {
        List<IdAndVersionProjection> otherVariablesInPanel = this.variableRepository
            .findAllIdsByPanelIdentifierAndIdNot(variable.getPanelIdentifier(), variable.getId());
        sameVariablesInPanel.put(variable.getId(), otherVariablesInPanel);
      }
    }
    dataForTemplate.put("questions", questionsMap);
    dataForTemplate.put("instruments", instrumentMap);
    dataForTemplate.put("firstTenValidResponses", firstTenValidResponses);
    dataForTemplate.put("lastTenValidResponses", lastTenValidResponses);
    dataForTemplate.put("sameVariablesInPanel", sameVariablesInPanel);

    return dataForTemplate;

  }

  /**
   * Inner class for get the variable ids as index for the variables hashmap.
   *
   * @author Daniel Katzberg
   *
   */
  static class VariableFunction implements Function<Variable, String> {
    /*
     * (non-Javadoc)
     *
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public String apply(Variable variable) {
      if (variable == null) {
        return null;
      }

      return variable.getId();
    }
  }
}

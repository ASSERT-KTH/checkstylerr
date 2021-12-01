package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.EvaluationResult;
import com.formulasearchengine.mathosphere.mlp.ml.WekaLearner;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.SimpleFeatureExtractorMapper;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;

import java.io.File;
import java.util.*;

public class MachineLearningModelGenerator {

  public static void main(String[] args) throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.from(args);
    find(config);
  }

  public static void find(MachineLearningDefinienExtractionConfig config) throws Exception {
    if (config.getInstancesFile() == null) {
      //parse wikipedia (subset) and process afterwards
      ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
      env.setParallelism(config.getParallelism());
      DataSource<String> source = FlinkMlpRelationFinder.readWikiDump(config, env);
      DataSet<ParsedWikiDocument> documents = source.flatMap(new TextExtractorMapper())
        .map(new TextAnnotatorMapper(config));
      ArrayList<GoldEntry> gold = (new Evaluator()).readGoldEntries(new File(config.getGoldFile()));
      DataSet<WikiDocumentOutput> instances = documents.map(new SimpleFeatureExtractorMapper(config, gold));
      //process parsed wikipedia
      DataSet<EvaluationResult> result = instances.reduceGroup(new WekaLearner(config));
      //write to kick off flink execution
      result.map(new JsonSerializerMapper<>())
        .writeAsText(config.getOutputDir() + "\\tmp", WriteMode.OVERWRITE);
      env.execute();
    } else {
      //just process
      findFromInstances(config);
    }
  }

  public static List<EvaluationResult> findFromInstances(MachineLearningDefinienExtractionConfig config) throws Exception {
    WekaLearner wekaLearner = new WekaLearner(config);
    return wekaLearner.processFromInstances();
  }
}

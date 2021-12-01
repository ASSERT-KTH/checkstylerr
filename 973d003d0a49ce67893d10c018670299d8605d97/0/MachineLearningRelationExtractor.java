package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienListConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.ml.WekaLearner;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mathosphere.mlp.text.SimpleFeatureExtractorMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.core.fs.FileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A combination of the {@link RelationExtractor} and {@link MachineLearningModelGenerator}.
 * The idea is to provide a program that can extract and list all identifier-definien
 * pairs without using a gold standard for comparison.
 *
 * @author Andre Greiner-Petter
 */
public class MachineLearningRelationExtractor {

    private static final Logger LOG = LogManager.getLogger( MachineLearningRelationExtractor.class.getName() );

    public static void start( MachineLearningDefinienListConfig config ){
        LOG.info("Start machine learning approach for listing identifier-definien pairs");
        // first, create a flink environment
        ExecutionEnvironment flinkEnv = ExecutionEnvironment.getExecutionEnvironment();
        flinkEnv.setParallelism( config.getParallelism() );

        LOG.debug("Read wikidump via flink");
        DataSource<String> dataSource = FlinkMlpRelationFinder.readWikiDump( config, flinkEnv );

        LOG.debug("Parse documents via flink");
        FlatMapOperator<String, RawWikiDocument> mapOperator = dataSource.flatMap(new TextExtractorMapper());

        LOG.debug("Open text annotator mapper");
        TextAnnotatorMapper annotatorMapper = new TextAnnotatorMapper(config);
        // ML approach doesn't create PosTagger here ... strange, so I will use it now.
        annotatorMapper.open(null);
        DataSet<ParsedWikiDocument> parsedDocuments = mapOperator.map( annotatorMapper );

        LOG.debug("Create feature Extractor without Gouldi");
        // use mlp instead of simple pattern matching as in machine learner...
        /*
         TODO compare: <qids are missing in createCandidatesMapper...>
         TODO mlp.contracts.CreateCandidatesMapper
         TODO mlp.text.     SimpleFeatureExtractorMapper
          */
        CreateCandidatesMapper mlp = new CreateCandidatesMapper(config);
        DataSet<WikiDocumentOutput> outputDocuments = parsedDocuments.map( mlp );

        LOG.debug("Write extracted identifiers and definiens before we apply machine learning");
        outputDocuments = writeMLPResults( config, outputDocuments );

        LOG.debug("Reduce groups by machine learning weka api");
        // deleted comparison with gouldi in this WekaLearner
        WekaLearner learner = new WekaLearner(config);
        DataSet<EvaluationResult> evaluationResults = outputDocuments.reduceGroup( learner );

        LOG.debug("Write results to the tmp.txt output file.");
        evaluationResults
                .map( new JsonSerializerMapper<>() )
                .writeAsText(
                        config.getOutputDir() + File.separator + "tmp.txt",
                        FileSystem.WriteMode.OVERWRITE
                );
        try {
            LOG.info("Execute flink environment");
            flinkEnv.execute();
        } catch ( Exception e ){
            LOG.error("Cannot execute flink environment.", e);
        }
    }

    public static int counter = 1;

    private static DataSet<WikiDocumentOutput> writeMLPResults( final FlinkMlpCommandConfig flinkConfig, DataSet<WikiDocumentOutput> dataSetWikiOuts ){
        return dataSetWikiOuts.map(
                (MapFunction<WikiDocumentOutput, WikiDocumentOutput>) wikiDocumentOutput
                        -> {
                    LOG.debug("Create not printer task and write current results of MLP to files.");
                    try (PrintWriter pw = createPrintWriter(flinkConfig)) {
                        LOG.info("Write WikiDocumentOutput information " + counter);
                        List<Relation> relations = wikiDocumentOutput.getRelations();
                        CSVPrinter printer = CSVFormat.DEFAULT.withRecordSeparator( System.lineSeparator() ).print(pw);
                        for (Relation r : relations) {
                            String[] record = {r.getIdentifier(), r.getDefinition(), Double.toString(r.getScore())};
                            printer.printRecord(record);
                        }
                        printer.flush();
                        pw.flush();
                    } catch ( IOException ioe ){
                        LOG.error("Cannot write results from the MLP process.", ioe);
                    }
                    return wikiDocumentOutput;
                });
    }

    private static PrintWriter createPrintWriter(FlinkMlpCommandConfig flinkConfig) throws IOException {
        Path outputDir = Paths.get(flinkConfig.getOutputDir());
        if (!Files.exists(outputDir) )
            Files.createDirectory(outputDir);

        Path outputF = outputDir.resolve("OutputFromMLP-" + (counter++) + ".csv");
        if (!Files.exists(outputF) )
            Files.createFile( outputF );

        return new PrintWriter(outputF.toFile());
    }
}

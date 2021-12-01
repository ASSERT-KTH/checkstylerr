package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.Serializable;

@Parameters(commandDescription = "Runs the MLP algorithm (on Flink)")
public class FlinkMlpCommandConfig extends BaseConfig implements Serializable {

  @Parameter(names = {"-in", "--inputDir"}, description = "path to the directory with wikidump", required = true)
  protected String dataset;

  @Parameter(names = {"-out", "--outputDir"}, description = "path to output directory", required = true)
  protected String outputdir;

  @Parameter(names = {"--threads"}, description = "how many parallel threads should be used")
  protected int parallelism = 1;

  public FlinkMlpCommandConfig() {
  }

  public static FlinkMlpCommandConfig test() {
    String dataset = "c:/tmp/mlp/input/";
    String outputdir = "c:/tmp/mlp/output/";

    FlinkMlpCommandConfig cfg = new FlinkMlpCommandConfig(dataset, outputdir);
    cfg.setUseTeXIdentifiers(false);
    return cfg;
  }

  public static FlinkMlpCommandConfig from(String[] args) {
    if (args.length == 0) {
      return test();
    }

    FlinkMlpCommandConfig config = new FlinkMlpCommandConfig();
    JCommander commander = new JCommander();
    commander.addObject(config);
    commander.parse(args);
    return config;
  }

  public FlinkMlpCommandConfig(String dataset, String outputdir) {
    this.dataset = dataset;
    this.outputdir = outputdir;
  }

  public FlinkMlpCommandConfig(String dataset, String outputdir, String model, String language, double alpha, double beta,
                               double gamma, double threshold, Boolean useTex) {
    super(model, language, alpha, beta, gamma, threshold, useTex);
    this.dataset = dataset;
    this.outputdir = outputdir;
  }

  public String getDataset() {
    return dataset;
  }

  public String getOutputDir() {
    return outputdir;
  }

  public int getParallelism() {
    return parallelism;
  }
}

package org.opencb.opencga.analysis.wrappers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.exec.Command;
import org.opencb.opencga.core.analysis.result.FileResult;
import org.opencb.opencga.core.annotations.Analysis;
import org.opencb.opencga.core.exception.AnalysisException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Analysis(id = PlinkWrapperAnalysis.ID, type = Analysis.AnalysisType.VARIANT)
public class PlinkWrapperAnalysis extends OpenCgaWrapperAnalysis {

    public final static String ID = "plink";
    public final static String PLINK_DOCKER_IMAGE = "gelog/plink";
    public final static String OUT_NAME = "plink";

    public final static String TPED_FILE_PARAM = "tpedFile";
    public final static String TFAM_FILE_PARAM = "tfamFile";
    public final static String COVAR_FILE_PARAM = "covarFile";

    protected void check() throws Exception {
        super.check();
    }

    @Override
    protected void run() throws Exception {
        step(() -> {
            String commandLine = getCommandLine();
            logger.info("Plink command line:" + commandLine);
            try {
                Set<String> filenamesBeforeRunning = new HashSet<>(getFilenames(getOutDir()));

                // Execute command and redirect stdout and stderr to the files: stdout.txt and stderr.txt
                Command cmd = new Command(getCommandLine())
                        .setOutputOutputStream(new DataOutputStream(new FileOutputStream(getOutDir().resolve(STDOUT_FILENAME).toFile())))
                        .setErrorOutputStream(new DataOutputStream(new FileOutputStream(getOutDir().resolve(STDERR_FILENAME).toFile())));

                cmd.run();

                // Add the output files to the analysis result file
                List<String> outNames = getFilenames(getOutDir());
                for (String name : outNames) {
                    if (!filenamesBeforeRunning.contains(name)) {
                        if (FileUtils.sizeOf(new File(getOutDir() + "/" + name)) > 0) {
                            FileResult.FileType fileType = FileResult.FileType.TAB_SEPARATED;
                            if (name.endsWith("txt") || name.endsWith("log")) {
                                fileType = FileResult.FileType.PLAIN_TEXT;
                            }
                            addFile(getOutDir().resolve(name), fileType);
                        }
                    }
                }
                // Check Plink errors by reading the stderr file
                File stderrFile = new File(getOutDir() + "/" + STDERR_FILENAME);
                if (FileUtils.sizeOf(stderrFile) > 0) {
                    throw new AnalysisException(StringUtils.join(FileUtils.readLines(stderrFile, Charset.defaultCharset()), ". "));
                }
            } catch (Exception e) {
                throw new AnalysisException(e);
            }
        });
    }

    @Override
    public String getDockerImageName() {
        return PLINK_DOCKER_IMAGE;
    }

    @Override
    public String getCommandLine() {
        StringBuilder sb = new StringBuilder("docker run ");
        sb.append(getMountParameters());
        sb.append(getDockerImageName());
        if (params.containsKey(DOCKER_IMAGE_VERSION_PARAM)) {
            sb.append(":").append(params.getString(DOCKER_IMAGE_VERSION_PARAM));
        }

        for (String key : params.keySet()) {
            if (checkParam(key)) {
                String value = params.getString(key);
                if (key.equals(COVAR_FILE_PARAM) && StringUtils.isNotEmpty(value)) {
                    String[] split = value.split("/");
                    value = split[split.length - 1];
                    sb.append(" --cov ").append(" ").append(DOCKER_INPUT_PATH).append("/").append(value);
                } else if (key.equals("out")) {
                    String[] split = value.split("/");
                    value = split[split.length - 1];
                    sb.append(" --out ").append(" ").append(DOCKER_OUTPUT_PATH).append("/").append(value);
                } else {
                    sb.append(" --").append(key);
                    if (StringUtils.isNotEmpty(value)) {
                        sb.append(" ").append(value);
                    }
                }
            }
        }
        if (!params.containsKey("out")) {
            sb.append(" --out ").append(DOCKER_OUTPUT_PATH).append("/").append(OUT_NAME);
        }
        // Input file management
        String tpedFilename = params.getString(TPED_FILE_PARAM);
        String prefix = new File(tpedFilename).getName().split("\\.")[0];
        sb.append(" --tfile ").append(DOCKER_INPUT_PATH).append("/").append(prefix);

        return sb.toString();
    }

    private String getMountParameters() {
        Set<String> sources = new HashSet<>();
        String[] names = {TPED_FILE_PARAM, TFAM_FILE_PARAM, COVAR_FILE_PARAM};
        for (String name : names) {
            if (params.containsKey(name) && StringUtils.isNotEmpty(params.getString(name))) {
                sources.add(new File(params.getString(name)).getParentFile().getAbsolutePath());
            }
        }

        StringBuilder sb = new StringBuilder();
        sources.forEach(s
                -> sb.append("--mount type=bind,source=\"").append(s).append("\",target=\"").append(DOCKER_INPUT_PATH).append("\" "));
        sb.append("--mount type=bind,source=\"")
                .append(getOutDir().toAbsolutePath()).append("\",target=\"").append(DOCKER_OUTPUT_PATH).append("\" ");

        return sb.toString();
    }

    private boolean checkParam(String key) {
        if (key.equals(DOCKER_IMAGE_VERSION_PARAM)
                || key.equals("noweb") || key.equals("file") || key.equals("bfile")
                || key.equals(TFAM_FILE_PARAM) || key.equals(TPED_FILE_PARAM)) {
            return false;
        }
        return true;
    }
}

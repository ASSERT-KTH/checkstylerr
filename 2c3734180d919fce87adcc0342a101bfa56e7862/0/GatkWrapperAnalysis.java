/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.opencga.analysis.wrappers.gatk;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.opencga.analysis.AnalysisUtils;
import org.opencb.opencga.analysis.tools.OpenCgaToolScopeStudy;
import org.opencb.opencga.catalog.managers.FileManager;
import org.opencb.opencga.core.exceptions.ToolException;
import org.opencb.opencga.core.models.variant.GatkWrapperParams;
import org.opencb.opencga.core.models.common.Enums;
import org.opencb.opencga.core.tools.annotations.Tool;
import org.opencb.opencga.core.tools.annotations.ToolParams;

import java.util.*;

import static org.opencb.opencga.core.api.ParamConstants.*;

@Tool(id = GatkWrapperAnalysis.ID, resource = Enums.Resource.VARIANT, description = GatkWrapperAnalysis.DESCRIPTION)
public class GatkWrapperAnalysis extends OpenCgaToolScopeStudy {

    public final static String ID = "gatk";
    public final static String DESCRIPTION = "GATK is a Genome Analysis Toolkit for variant discovery in high-throughput sequencing data. "
            + GATK_COMMAND_DESCRIPTION;

    @ToolParams
    protected final GatkWrapperParams analysisParams = new GatkWrapperParams();

    protected void check() throws Exception {
        super.check();

        if (StringUtils.isEmpty(analysisParams.getCommand())) {
            throw new ToolException("Missing Gatk command.");
        }

        if (!AnalysisUtils.isSupportedCommand(GATK_COMMANDS_SUPPORTED)) {
            throw new ToolException("Gatk command '" + analysisParams.getCommand() + "' is not available. Supported commands are "
                    + GATK_COMMANDS_SUPPORTED);
        }

        // Get files from catalog
        FileManager fileManager = catalogManager.getFileManager();
        if (MapUtils.isNotEmpty(analysisParams.getGatkParams())) {
            Set<String> fileParams = getFileParamNames(analysisParams.getCommand());

            Map<String, String> updatedMap = new HashMap<>();
            for (Map.Entry<String, String> entry : analysisParams.getGatkParams().entrySet()) {
                if (fileParams.contains(entry.getKey())) {
                    updatedMap.put(entry.getKey(), AnalysisUtils.getCatalogFile(entry.getValue(), study, fileManager, token)
                            .getUri().getPath());
                }
            }
            if (MapUtils.isNotEmpty(updatedMap)) {
                analysisParams.getGatkParams().putAll(updatedMap);
            }
        }
    }

    @Override
    protected void run() throws Exception {
        setUpStorageEngineExecutor(study);

        step(() -> {
            if (MapUtils.isNotEmpty(analysisParams.getGatkParams())) {
                executorParams.appendAll(analysisParams.getGatkParams());
            }

            getToolExecutor(GatkWrapperAnalysisExecutor.class)
                    .setCommand(analysisParams.getCommand())
                    .execute();
        });
    }

    public static Set<String> getFileParamNames(String command) {
        switch (command) {
            case "HaplotypeCaller":
                return new HashSet<>(Arrays.asList("R", "I"));
            default:
                return new HashSet<>();
        }
    }
}

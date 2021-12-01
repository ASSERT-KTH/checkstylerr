package org.opencb.opencga.core.models.clinical;

import org.opencb.biodata.models.clinical.interpretation.ClinicalProperty;
import org.opencb.opencga.core.tools.ToolParams;

import java.util.List;

public class TieringInterpretationAnalysisParams extends ToolParams {
    public static final String DESCRIPTION = "GEL tiering interpretation analysis params";

    private String clinicalAnalysis;
    private String panel;
    private ClinicalProperty.Penetrance penetrance;
    private int maxLowCoverage;
    private boolean includeLowCoverage;

    public TieringInterpretationAnalysisParams() {
    }

    public TieringInterpretationAnalysisParams(String clinicalAnalysis, String panel, ClinicalProperty.Penetrance penetrance,
                                               int maxLowCoverage, boolean includeLowCoverage) {
        this.clinicalAnalysis = clinicalAnalysis;
        this.panel = panel;
        this.penetrance = penetrance;
        this.maxLowCoverage = maxLowCoverage;
        this.includeLowCoverage = includeLowCoverage;
    }

    public String getClinicalAnalysis() {
        return clinicalAnalysis;
    }

    public TieringInterpretationAnalysisParams setClinicalAnalysis(String clinicalAnalysis) {
        this.clinicalAnalysis = clinicalAnalysis;
        return this;
    }

    public String getPanel() {
        return panel;
    }

    public TieringInterpretationAnalysisParams setPanel(String panel) {
        this.panel = panel;
        return this;
    }

    public ClinicalProperty.Penetrance getPenetrance() {
        return penetrance;
    }

    public TieringInterpretationAnalysisParams setPenetrance(ClinicalProperty.Penetrance penetrance) {
        this.penetrance = penetrance;
        return this;
    }

    public int getMaxLowCoverage() {
        return maxLowCoverage;
    }

    public TieringInterpretationAnalysisParams setMaxLowCoverage(int maxLowCoverage) {
        this.maxLowCoverage = maxLowCoverage;
        return this;
    }

    public boolean isIncludeLowCoverage() {
        return includeLowCoverage;
    }

    public TieringInterpretationAnalysisParams setIncludeLowCoverage(boolean includeLowCoverage) {
        this.includeLowCoverage = includeLowCoverage;
        return this;
    }
}

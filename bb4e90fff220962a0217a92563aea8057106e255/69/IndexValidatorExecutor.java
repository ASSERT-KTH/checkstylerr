package org.slc.sli.ingestion.validation;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slc.sli.ingestion.reporting.ReportStats;
import org.slc.sli.ingestion.reporting.Source;
import org.slc.sli.ingestion.reporting.impl.JobSource;
import org.slc.sli.ingestion.reporting.impl.LoggingMessageReport;
import org.slc.sli.ingestion.reporting.impl.SimpleReportStats;

@Component
public class IndexValidatorExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(IndexValidatorExecutor.class);

    @Autowired
    private Validator<?> systemValidatorStartUp;

    @Autowired
    private LoggingMessageReport loggingMessageReport;

    public ReportStats checkNonTenantIndexes() throws IndexValidationException{
        loggingMessageReport.setLogger(LOG);
        ReportStats reportStats = new SimpleReportStats();
        Source source = new JobSource("IngestionService");
        boolean indexValidated = systemValidatorStartUp.isValid(null, loggingMessageReport, reportStats, source);
        return reportStats;
    }

    public void setValidator(Validator<?> systemValidator)
    {
        this.systemValidatorStartUp = systemValidator;
    }

    public void setLoggingMessageReport(LoggingMessageReport loggingMessageReport)
    {
        this.loggingMessageReport = loggingMessageReport;
    }
}

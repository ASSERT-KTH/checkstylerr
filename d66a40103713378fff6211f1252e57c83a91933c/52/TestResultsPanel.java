package fi.helsinki.cs.tmc.intellij.ui.testresults;

import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.intellij.ui.components.JBScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class TestResultsPanel extends JPanel {

    private static final int MARGIN = 5;

    private static final Logger logger = LoggerFactory.getLogger(TestResultsPanel.class);
    private GridBagConstraints resultsListConstraints;
    private TestResultProgressBar progressBar;
    private JPanel resultsList;

    public TestResultsPanel() {
        createLayout();
    }

    private void createLayout() {
        logger.info("Creating layout. @TestResultsPanel");
        this.setLayout(new GridBagLayout());

        // we want the progress bar to be separate from the scroll panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addProgressBar(gbc);


        final JScrollPane scrollPane1 = new JBScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane1.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollPane1, gbc);

        resultsList = new JPanel();
        resultsList.setLayout(new GridBagLayout());
        scrollPane1.setViewportView(resultsList);
    }

    private void addProgressBar(GridBagConstraints gbc) {
        JPanel barContainer = new JPanel();
        barContainer.setLayout(new BorderLayout());
        createProgressBar();
        barContainer.add(progressBar, BorderLayout.CENTER);
        this.add(barContainer, gbc);
    }

    private void createProgressBar() {
        progressBar = new TestResultProgressBar();
        progressBar.setVisible(false); // invisible until first results come in
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(true);
    }

    public void showResults(List<TestResult> testResults, ValidationResult validationResult) {
        logger.info("Showing all test results. @TestResultsPanel");

        resultsList.removeAll();

        createConstraints();
        if (validationResult != null) {
            // not every result returns with validation errors
            createValidationRows(validationResult);
        }
        createTestRows(testResults);

        // force rows to min width, get rid of vertical scaling behavior
        resultsListConstraints.weighty = 1.0;
        // glue must be added *after* the rows so that it fills the remaining
        // space from *below*
        this.resultsList.add(Box.createVerticalGlue(), resultsListConstraints);

        boolean passed = validationResult == null
                || validationResult.getValidationErrors() == null
                || validationResult.getValidationErrors().size() == 0;
        progressBar.validationPass(passed);
        progressBar.setValue((int)(100 * testPassRatio(testResults)));
        progressBar.setVisible(true);

        this.revalidate();
        this.repaint();
    }

    private void createConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // don't stretch vertically
        gbc.insets.top = MARGIN;
        this.resultsListConstraints = gbc;
    }

    private void createTestRows(List<TestResult> testResults) {
        for (TestResult result : testResults) {
            String details = getDetails(result);
            TestResultRow row = createTestRow(result, details);
            resultsList.add(row, resultsListConstraints);
        }
    }

    private String getDetails(TestResult result) {
        List<String> detailsRows = getDetailsOrMessageRows(result);
        if (detailsRows == null) {
            return null;
        }
        return String.join("\n", detailsRows);
    }

    private List<String> getDetailsOrMessageRows(TestResult res) {
        return res.getDetailedMessage().size() > 0 ? res.getDetailedMessage() : res.getException();
    }

    private TestResultRow createTestRow(TestResult result, String details) {
        if (result.isSuccessful()) {
            return TestResultRow.createSuccessfulTestRow(
                    result.getName(),
                    result.getMessage(),
                    details);
        } else {
            return TestResultRow.createFailedTestRow(
                    result.getName(),
                    result.getMessage(),
                    details);
        }
    }

    private void createValidationRows(ValidationResult validationResult) {
        validationResult.getValidationErrors().forEach((key, value) -> createValidationRowForEachError(key, value));
    }

    private void createValidationRowForEachError(File file, List<ValidationError> errors) {

        for (ValidationError error : errors) {
            String message = String.format(
                    "Line %d:    %s",
                    error.getLine(),
                    error.getMessage());

            TestResultRow row =
                    TestResultRow.createValidationRow(
                        file.getPath(),
                        message,
                        error.getSourceName());
            resultsList.add(row, resultsListConstraints);
        }
    }

    private double testPassRatio(List<TestResult> tests) {
        if (tests.size() == 0) {
            return 1;
        }
        return tests.stream().filter(TestResult::isSuccessful).count() / (double) tests.size();
    }
}

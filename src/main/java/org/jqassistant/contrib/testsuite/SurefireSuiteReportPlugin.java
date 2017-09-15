package org.jqassistant.contrib.testsuite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;

public class SurefireSuiteReportPlugin implements ReportPlugin {

    public static final String REPORT_ID = "surefire-suite";
    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireSuiteReportPlugin.class);

    private static final String PROPERTY_DIRECTORY = "testsuite.report.directory";
    private static final String PROPERTY_ARTIFACT_COLUMN = "testsuite.surefire.artifactColumn";
    private static final String PROPERTY_TESTS_COLUMN = "testsuite.surefire.testsColumn";

    private static final String DEFAULT_DIRECTORY_DEFAULT = "jqassistant/report/testsuite";
    private static final String DEFAULT_ARTIFACT_COLUMN = "Artifact";
    private static final String DEFAULT_TESTS_COLUMN = "Tests";

    private File reportDirectory;

    public void initialize() throws ReportException {
    }

    public void configure(Map<String, Object> properties) throws ReportException {
        String directoryName = (String) properties.get(PROPERTY_DIRECTORY);
        this.reportDirectory = directoryName != null ? new File(directoryName) : new File(DEFAULT_DIRECTORY_DEFAULT);
        if (this.reportDirectory.mkdirs()) {
            LOGGER.info("Created directory '" + this.reportDirectory.getAbsolutePath() + "'.");
        }
    }

    public void begin() throws ReportException {
    }

    public void end() throws ReportException {
    }

    public void beginConcept(Concept concept) throws ReportException {
    }

    public void endConcept() throws ReportException {
    }

    public void beginGroup(Group group) throws ReportException {
    }

    public void endGroup() throws ReportException {
    }

    public void beginConstraint(Constraint constraint) throws ReportException {
    }

    public void endConstraint() throws ReportException {
    }

    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        Report report = result.getRule().getReport();
        Set<String> selectedTypes = report.getSelectedTypes();
        if (selectedTypes != null && selectedTypes.contains(REPORT_ID)) {
            Properties properties = report.getProperties();
            String artifactColumn = properties.getProperty(PROPERTY_ARTIFACT_COLUMN, DEFAULT_ARTIFACT_COLUMN);
            String testsColumn = properties.getProperty(PROPERTY_TESTS_COLUMN, DEFAULT_TESTS_COLUMN);
            for (Map<String, Object> row : result.getRows()) {
                String artifactName = getColumnValue(row, artifactColumn, String.class);
                Iterable<String> testClasses = getColumnValue(row, testsColumn, Iterable.class);
                if (artifactName == null) {
                    LOGGER.warn("Cannot determine artifact from column '" + artifactColumn + "'.");
                } else if (testClasses == null) {
                    LOGGER.warn("Cannot determine tests from column '" + testsColumn + "'.");
                } else {
                    File file = new File(reportDirectory, artifactName + ".includes");
                    try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                        for (String testClass : testClasses) {
                            writer.println(testClass.substring(1)); // strip leading slash
                        }
                    } catch (IOException e) {
                        throw new ReportException("Cannot write tests to '" + file.getAbsolutePath() + "'");
                    }
                }
            }
        }
    }

    private <T> T getColumnValue(Map<String, Object> row, String name, Class<T> expectedType) throws ReportException {
        Object value = row.get(name);
        if (value != null) {
            Class<?> valueType = value.getClass();
            if (!expectedType.isAssignableFrom(expectedType)) {
                throw new ReportException("Expecting a " + expectedType.getName() + " but got '" + value + "' of type '" + valueType.getName() + "'.");
            }
        }
        return expectedType.cast(value);
    }
}

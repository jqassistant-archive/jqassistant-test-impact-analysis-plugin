package org.jqassistant.contrib.plugin.testimpactanalysis;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ClassTypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link ReportPlugin} that creates files containing source file names of
 * test classes for execution using Maven Surefire Plugin.
 */
public class SurefireSuiteReportPlugin implements ReportPlugin {

    public static final String REPORT_TYPE = "surefire-suite";
    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireSuiteReportPlugin.class);

    private static final String PROPERTY_DIRECTORY = "testImpactAnalysis.report.directory";
    private static final String PROPERTY_ARTIFACT_COLUMN = "testImpactAnalysis.surefire.artifactColumn";
    private static final String PROPERTY_TESTS_COLUMN = "testImpactAnalysis.surefire.testsColumn";
    private static final String PROPERTY_REPORT_FILE = "testImpactAnalysis.surefire.file";

    private static final String DEFAULT_ARTIFACT_COLUMN = "Artifact";
    private static final String DEFAULT_TESTS_COLUMN = "Tests";
    private static final String DEFAULT_REPORT_FILE = "surefire-tests";

    private File reportDirectory;

    private File reportFile;

    private String artifactColumn = DEFAULT_ARTIFACT_COLUMN;

    private String testsColumn = DEFAULT_TESTS_COLUMN;

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        String directoryName = (String) properties.get(PROPERTY_DIRECTORY);
        this.reportDirectory = directoryName != null ? new File(directoryName) : reportContext.getReportDirectory(REPORT_TYPE);
        if (this.reportDirectory.mkdirs()) {
            LOGGER.info("Created directory '" + this.reportDirectory.getAbsolutePath() + "'.");
        }
        String reportFileName = (String) properties.get(PROPERTY_REPORT_FILE);
        this.reportFile = reportFileName != null ? new File(this.reportDirectory, reportFileName) : null;
        if (properties.containsKey(PROPERTY_ARTIFACT_COLUMN)) {
            this.artifactColumn = (String) properties.get(PROPERTY_ARTIFACT_COLUMN);
        }
        if (properties.containsKey(PROPERTY_TESTS_COLUMN)) {
            this.testsColumn = (String) properties.get(PROPERTY_TESTS_COLUMN);
        }
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        Set<File> files = new HashSet<>();
        for (Map<String, Object> row : result.getRows()) {
            ArtifactDescriptor artifactDescriptor = getColumnValue(row, artifactColumn, ArtifactDescriptor.class);
            Iterable<ClassTypeDescriptor> testClasses = getColumnValue(row, testsColumn, Iterable.class);
            File file = getReportFile(artifactDescriptor);
            if (testClasses == null) {
                LOGGER.warn("Cannot determine tests from column '" + testsColumn + "'.");
            } else {
                boolean append = !files.add(file);
                writeTests(file, append, testClasses);
            }
        }
    }

    /**
     * Extract the value of a column providing an expected type.
     *
     * @param row          The row.
     * @param name         The name of the column.
     * @param expectedType The expected type.
     * @param <T>          The expected type.
     * @return The value.
     * @throws ReportException If the value type does not match the expected type.
     */
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

    /**
     * Determines the report file for the given artifact.
     *
     * @param artifactDescriptor The artifact descriptor.
     * @return The report file.
     */
    private File getReportFile(ArtifactDescriptor artifactDescriptor) {
        File file;
        if (this.reportFile != null) {
            file = this.reportFile;
        } else if (artifactDescriptor != null) {
            file = new File(reportDirectory, artifactDescriptor.getName());
        } else {
            file = new File(reportDirectory, DEFAULT_REPORT_FILE);
        }
        return file;
    }

    /**
     * Writes test classes to the given file.
     *
     * @param file        The file.
     * @param append      If <code>true</code> the test classes will be appended if the file
     *                    already exists.
     * @param testClasses The test classes.
     * @throws ReportException If the file cannot be written.
     */
    private void writeTests(File file, boolean append, Iterable<ClassTypeDescriptor> testClasses) throws ReportException {
        LOGGER.info((append ? "Appending" : "Writing") + " tests to '" + file.getPath() + "'.");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, append))) {
            for (ClassTypeDescriptor testClass : testClasses) {
                String sourceFileName = testClass.getSourceFileName();
                String name = testClass.getName();
                String fullQualifiedName = testClass.getFullQualifiedName();
                String packageName = fullQualifiedName.substring(0, fullQualifiedName.length() - name.length());
                String fullSourceFileName = packageName.replace('.', '/') + sourceFileName;
                writer.println(fullSourceFileName);
                LOGGER.info("\t" + testClass.getFullQualifiedName() + " (" + fullSourceFileName + ")");
            }
        } catch (IOException e) {
            throw new ReportException("Cannot write tests to '" + file.getAbsolutePath() + "'", e);
        }
    }
}

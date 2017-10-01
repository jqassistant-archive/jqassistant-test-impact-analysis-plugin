package org.jqassistant.contrib.plugin.testimpactanalysis;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.jqassistant.contrib.plugin.testimpactanalysis.SurefireSuiteReportPlugin.REPORT_ID;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.report.Artifact1Test2;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.report.Artifact2Test1;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.report.Artifact1Test1;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Report;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ClassTypeDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class SurefireSuiteReportPluginTest {

    private static final File REPORT_DIRECTORY = new File("target/testimpactanalysis");

    private SurefireSuiteReportPlugin plugin = new SurefireSuiteReportPlugin();

    @Before
    public void init() throws IOException {
        FileUtils.deleteDirectory(REPORT_DIRECTORY);
    }

    @Test
    public void testsPerArtifact() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("Artifact", "artifact1", "Tests", Artifact1Test1.class, Artifact1Test2.class));
        rows.add(createRow("Artifact", "artifact2", "Tests", Artifact2Test1.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        plugin.configure(getConfiguration());

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact1");
        assertThat(artifact1Testsuite.size(), equalTo(2));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
        assertThat(artifact1Testsuite.get(1), equalTo(getExpectedSourceFileName(Artifact1Test2.class)));
        List<String> artifact2Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact2");
        assertThat(artifact2Testsuite.size(), equalTo(1));
        assertThat(artifact2Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact2Test1.class)));
    }

    @Test
    public void ambiguousArtifacts() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("Artifact", "artifact", "Tests", Artifact1Test1.class));
        rows.add(createRow("Artifact", "artifact", "Tests", Artifact1Test2.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        plugin.configure(getConfiguration());

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact");
        assertThat(artifact1Testsuite.size(), equalTo(2));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
        assertThat(artifact1Testsuite.get(1), equalTo(getExpectedSourceFileName(Artifact1Test2.class)));
    }

    @Test
    public void customColumns() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("a", "artifact", "t", Artifact1Test1.class));
        Properties reportProperties = new Properties();
        Result<? extends ExecutableRule> result = getResult(rows, reportProperties);
        Map<String, Object> configuration = getConfiguration();
        configuration.put("testImpactAnalysis.surefire.artifactColumn", "a");
        configuration.put("testImpactAnalysis.surefire.testsColumn", "t");
        plugin.configure(configuration);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact");
        assertThat(artifact1Testsuite.size(), equalTo(1));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
    }

    @Test
    public void withoutArtifact() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow(null, null, "Tests", Artifact1Test1.class, Artifact1Test2.class, Artifact2Test1.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        plugin.configure(getConfiguration());

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "surefire-tests");
        assertThat(artifact1Testsuite.size(), equalTo(3));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
        assertThat(artifact1Testsuite.get(1), equalTo(getExpectedSourceFileName(Artifact1Test2.class)));
        assertThat(artifact1Testsuite.get(2), equalTo(getExpectedSourceFileName(Artifact2Test1.class)));
    }

    @Test
    public void customReportFile() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("Artifact", "artifact1", "Tests", Artifact1Test1.class, Artifact1Test2.class));
        rows.add(createRow("Artifact", "artifact2", "Tests", Artifact2Test1.class));
        Properties reportProperties = new Properties();
        Result<? extends ExecutableRule> result = getResult(rows, reportProperties);
        Map<String, Object> configuration = getConfiguration();
        configuration.put("testImpactAnalysis.surefire.file", "tests");
        plugin.configure(configuration);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "tests");
        assertThat(artifact1Testsuite.size(), equalTo(3));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
        assertThat(artifact1Testsuite.get(1), equalTo(getExpectedSourceFileName(Artifact1Test2.class)));
        assertThat(artifact1Testsuite.get(2), equalTo(getExpectedSourceFileName(Artifact2Test1.class)));
    }

    private Map<String, Object> createRow(String artifactColumn, String artifactName, String testsColumn, Class<?>... testClasses) {
        Map<String, Object> row = new HashMap<>();
        ArtifactDescriptor artifactDescriptor = mock(ArtifactDescriptor.class);
        when(artifactDescriptor.getName()).thenReturn(artifactName);
        row.put(artifactColumn, artifactDescriptor);
        List<Object> tests = new ArrayList<>();
        for (Class<?> testClass : testClasses) {
            ClassTypeDescriptor type = mock(ClassTypeDescriptor.class);
            when(type.getSourceFileName()).thenReturn(testClass.getSimpleName() + ".java");
            when(type.getName()).thenReturn(testClass.getSimpleName());
            when(type.getFullQualifiedName()).thenReturn(testClass.getName());
            tests.add(type);
        }
        row.put(testsColumn, tests);
        return row;
    }

    private Result<? extends ExecutableRule> getResult(List<Map<String, Object>> rows, Properties reportProperties) {
        Result<? extends ExecutableRule> result = mock(Result.class);

        Concept concept = mock(Concept.class);
        when(result.getRule()).thenReturn(concept);

        Report report = mock(Report.class);
        when(concept.getReport()).thenReturn(report);
        when(report.getSelectedTypes()).thenReturn(new HashSet<>(asList(REPORT_ID)));
        when(report.getProperties()).thenReturn(reportProperties);

        when(result.getRows()).thenReturn(rows);
        return result;
    }

    private Map<String, Object> getConfiguration() throws ReportException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("testImpactAnalysis.report.directory", REPORT_DIRECTORY.getAbsolutePath());
        return properties;
    }

    private List<String> getTestsuiteReport(File reportDirectory, String fileName) throws IOException {
        File testArtifactSuite = new File(reportDirectory, fileName);
        assertThat(testArtifactSuite.exists(), equalTo(true));
        List<String> testsuite = new ArrayList<>();
        try (LineNumberReader reader = new LineNumberReader(new FileReader(testArtifactSuite))) {
            String line;
            while ((line = reader.readLine()) != null) {
                testsuite.add(line);
            }
        }
        return testsuite;
    }

    private String getExpectedSourceFileName(Class<?> type) {
        return type.getName().replace('.', '/') + ".java";
    }

}

package org.jqassistant.contrib.testsuite;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

import org.jqassistant.contrib.testsuite.set.Artifact1Test1;
import org.jqassistant.contrib.testsuite.set.Artifact1Test2;
import org.jqassistant.contrib.testsuite.set.Artifact2Test1;
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

    private SurefireSuiteReportPlugin plugin = new SurefireSuiteReportPlugin();

    @Test
    public void testsPerArtifact() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("Artifact", "artifact1", "Tests", Artifact1Test1.class, Artifact1Test2.class));
        rows.add(createRow("Artifact", "artifact2", "Tests", Artifact2Test1.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        File reportDirectory = new File("target/testsuite");
        configure(reportDirectory);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(reportDirectory, "artifact1");
        assertThat(artifact1Testsuite.size(), equalTo(2));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
        assertThat(artifact1Testsuite.get(1), equalTo(getExpectedSourceFileName(Artifact1Test2.class)));
        List<String> artifact2Testsuite = getTestsuiteReport(reportDirectory, "artifact2");
        assertThat(artifact2Testsuite.size(), equalTo(1));
        assertThat(artifact2Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact2Test1.class)));
    }

    @Test
    public void customColumns() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("a", "artifact", "t", Artifact1Test1.class));
        Properties reportProperties = new Properties();
        reportProperties.setProperty("testsuite.surefire.artifactColumn", "a");
        reportProperties.setProperty("testsuite.surefire.testsColumn", "t");
        Result<? extends ExecutableRule> result = getResult(rows, reportProperties);
        File reportDirectory = new File("target/testsuite");
        configure(reportDirectory);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(reportDirectory, "artifact");
        assertThat(artifact1Testsuite.size(), equalTo(1));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
    }

    @Test
    public void withoutArtifact() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow(null, null, "Tests", Artifact1Test1.class, Artifact1Test2.class, Artifact2Test1.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        File reportDirectory = new File("target/testsuite");
        configure(reportDirectory);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(reportDirectory, "surefire-tests");
        assertThat(artifact1Testsuite.size(), equalTo(3));
        assertThat(artifact1Testsuite.get(0), equalTo(getExpectedSourceFileName(Artifact1Test1.class)));
        assertThat(artifact1Testsuite.get(1), equalTo(getExpectedSourceFileName(Artifact1Test2.class)));
        assertThat(artifact1Testsuite.get(2), equalTo(getExpectedSourceFileName(Artifact2Test1.class)));
    }

    @Test
    public void withoutCustomArtifact() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow(null, null, "Tests", Artifact1Test1.class, Artifact1Test2.class, Artifact2Test1.class));
        Properties reportProperties = new Properties();
        reportProperties.setProperty("testsuite.surefire.file", "tests");
        Result<? extends ExecutableRule> result = getResult(rows, reportProperties);
        File reportDirectory = new File("target/testsuite");
        configure(reportDirectory);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(reportDirectory, "tests");
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
        when(report.getSelectedTypes()).thenReturn(new HashSet<>(asList(SurefireSuiteReportPlugin.REPORT_ID)));
        when(report.getProperties()).thenReturn(reportProperties);

        when(result.getRows()).thenReturn(rows);
        return result;
    }

    private void configure(File reportDirectory) throws ReportException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("testsuite.report.directory", reportDirectory.getAbsolutePath());
        plugin.configure(properties);
    }

    private List<String> getTestsuiteReport(File reportDirectory, String fileName) throws IOException {
        File testArtifactSuite = new File(reportDirectory, fileName);
        assertThat(testArtifactSuite.exists(), equalTo(true));
        LineNumberReader reader = new LineNumberReader(new FileReader(testArtifactSuite));
        List<String> testsuite = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            testsuite.add(line);
        }
        return testsuite;
    }

    private String getExpectedSourceFileName(Class<?> type) {
        return type.getName().replace('.', '/') + ".java";
    }

}

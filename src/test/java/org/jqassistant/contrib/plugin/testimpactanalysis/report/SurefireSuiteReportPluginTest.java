package org.jqassistant.contrib.plugin.testimpactanalysis.report;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Report;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ClassTypeDescriptor;
import org.apache.commons.io.FileUtils;
import org.jqassistant.contrib.plugin.testimpactanalysis.SurefireSuiteReportPlugin;
import org.jqassistant.contrib.plugin.testimpactanalysis.report.set.Artifact1Test1;
import org.jqassistant.contrib.plugin.testimpactanalysis.report.set.Artifact1Test2;
import org.jqassistant.contrib.plugin.testimpactanalysis.report.set.Artifact2Test1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jqassistant.contrib.plugin.testimpactanalysis.SurefireSuiteReportPlugin.REPORT_TYPE;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SurefireSuiteReportPluginTest {

    private static final File REPORT_DIRECTORY = new File("target/testimpactanalysis");

    private SurefireSuiteReportPlugin plugin = new SurefireSuiteReportPlugin();

    @Mock
    private ReportContext reportContext;

    @BeforeEach
    public void init() throws IOException {
        FileUtils.deleteDirectory(REPORT_DIRECTORY);
        doReturn(REPORT_DIRECTORY).when(reportContext).getReportDirectory(SurefireSuiteReportPlugin.REPORT_TYPE);
    }

    @Test
    public void testsPerArtifact() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("Artifact", "artifact1", "Tests", Artifact1Test1.class, Artifact1Test2.class));
        rows.add(createRow("Artifact", "artifact2", "Tests", Artifact2Test1.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        plugin.configure(reportContext, getConfiguration());

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact1");
        assertThat(artifact1Testsuite.size()).isEqualTo(2);
        assertThat(artifact1Testsuite.get(0)).isEqualTo(getExpectedSourceFileName(Artifact1Test1.class));
        assertThat(artifact1Testsuite.get(1)).isEqualTo(getExpectedSourceFileName(Artifact1Test2.class));
        List<String> artifact2Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact2");
        assertThat(artifact2Testsuite.size()).isEqualTo(1);
        assertThat(artifact2Testsuite.get(0)).isEqualTo(getExpectedSourceFileName(Artifact2Test1.class));
    }

    @Test
    public void ambiguousArtifacts() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow("Artifact", "artifact", "Tests", Artifact1Test1.class));
        rows.add(createRow("Artifact", "artifact", "Tests", Artifact1Test2.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        plugin.configure(reportContext, getConfiguration());

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact");
        assertThat(artifact1Testsuite.size()).isEqualTo(2);
        assertThat(artifact1Testsuite.get(0)).isEqualTo(getExpectedSourceFileName(Artifact1Test1.class));
        assertThat(artifact1Testsuite.get(1)).isEqualTo(getExpectedSourceFileName(Artifact1Test2.class));
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
        plugin.configure(reportContext, configuration);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "artifact");
        assertThat(artifact1Testsuite.size()).isEqualTo(1);
        assertThat(artifact1Testsuite.get(0)).isEqualTo(getExpectedSourceFileName(Artifact1Test1.class));
    }

    @Test
    public void withoutArtifact() throws ReportException, IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createRow(null, null, "Tests", Artifact1Test1.class, Artifact1Test2.class, Artifact2Test1.class));
        Result<? extends ExecutableRule> result = getResult(rows, new Properties());
        plugin.configure(reportContext, getConfiguration());

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "surefire-tests");
        assertThat(artifact1Testsuite.size()).isEqualTo(3);
        assertThat(artifact1Testsuite.get(0)).isEqualTo(getExpectedSourceFileName(Artifact1Test1.class));
        assertThat(artifact1Testsuite.get(1)).isEqualTo(getExpectedSourceFileName(Artifact1Test2.class));
        assertThat(artifact1Testsuite.get(2)).isEqualTo(getExpectedSourceFileName(Artifact2Test1.class));
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
        plugin.configure(reportContext, configuration);

        plugin.setResult(result);

        List<String> artifact1Testsuite = getTestsuiteReport(REPORT_DIRECTORY, "tests");
        assertThat(artifact1Testsuite.size()).isEqualTo(3);
        assertThat(artifact1Testsuite.get(0)).isEqualTo(getExpectedSourceFileName(Artifact1Test1.class));
        assertThat(artifact1Testsuite.get(1)).isEqualTo(getExpectedSourceFileName(Artifact1Test2.class));
        assertThat(artifact1Testsuite.get(2)).isEqualTo(getExpectedSourceFileName(Artifact2Test1.class));
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
        doReturn(concept).when(result).getRule();

        Report report = mock(Report.class);
        when(concept.getReport()).thenReturn(report);
        when(report.getSelectedTypes()).thenReturn(new HashSet<>(asList(REPORT_TYPE)));
        when(report.getProperties()).thenReturn(reportProperties);

        when(result.getRows()).thenReturn(rows);
        return result;
    }

    private Map<String, Object> getConfiguration() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("testImpactAnalysis.report.directory", REPORT_DIRECTORY.getAbsolutePath());
        return properties;
    }

    private List<String> getTestsuiteReport(File reportDirectory, String fileName) throws IOException {
        File testArtifactSuite = new File(reportDirectory, fileName);
        assertThat(testArtifactSuite.exists()).isEqualTo(true);
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

package org.jqassistant.contrib.plugin.testimpactanalysis;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitChangeDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitFileDescriptor;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public abstract class AbstractTestImpactAnalysisRuleTest extends AbstractJavaPluginIT {

    protected void verify(Class<?> changedType) throws Exception {
        scanClassPathDirectory("a1", getClassesDirectory(TestsAffectedByCurrentGitBranchTest.class));
        createGitHistory(changedType);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("testImpactAnalysisGitBaseBranch","test-feature");
        Result<Concept> result = applyConcept(getConcept(), parameters);

        assertThat(result.getStatus(), equalTo(SUCCESS));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        store.beginTransaction();
        ArtifactDescriptor artifact = (ArtifactDescriptor) row.get("Artifact");
        assertThat(artifact, notNullValue());
        assertThat(artifact.getFullQualifiedName(), equalTo("a1"));
        List<TypeDescriptor> tests = (List<TypeDescriptor>) row.get("Tests");
        assertThat(tests, notNullValue());
        assertThat(tests, hasItems(typeDescriptor(TypeTest.class)));
        assertThat(tests, not(hasItems(typeDescriptor(OtherTypeTest.class))));
        assertThat(store.executeQuery("MATCH (t:Type:Changed) RETURN t").getSingleResult().get("t", TypeDescriptor.class), typeDescriptor(changedType));
        List<TypeDescriptor> affectedTests = store.executeQuery("MATCH (t:Type:Test:Affected) RETURN collect(t) as affectedTests").getSingleResult()
                .get("affectedTests", List.class);
        assertThat(affectedTests, hasItems(typeDescriptor(TypeTest.class), typeDescriptor(TestsAffectedByCurrentGitBranchTest.class)));
        store.commitTransaction();
    }

    protected abstract String getConcept();

    protected abstract void createGitHistory(Class<?> changedType);

    private String getSourceFileName(Class<?> type) {
        String sourceDirectory = "/src/test/java/";
        return sourceDirectory + type.getName().replace('.', '/') + ".java";
    }

    protected GitCommitDescriptor createCommit(Class<?>... types) {
        GitCommitDescriptor commit = store.create(GitCommitDescriptor.class);
        for (Class<?> type : types) {
            GitChangeDescriptor change = store.create(GitChangeDescriptor.class);
            GitFileDescriptor file = store.create(GitFileDescriptor.class);
            file.setRelativePath(getSourceFileName(type));
            change.setModifies(file);
            commit.getFiles().add(change);
        }
        return commit;
    }
}

package org.jqassistant.contrib.plugin.testimpactanalysis.impact;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.jqassistant.contrib.plugin.testimpactanalysis.AbstractGitRuleIT;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.OtherTypeTest;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.TypeTest;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

abstract class AbstractTestImpactAnalysisRuleIT extends AbstractGitRuleIT {

    protected void verify(Class<?> changedType, String concept, Map<String, String> parameters) throws Exception {
        scanClassPathDirectory("a1", getClassesDirectory(TestsAffectedByCurrentGitBranchIT.class));

        Result<Concept> result = applyConcept(concept, parameters);

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
        assertThat(affectedTests, hasItems(typeDescriptor(TypeTest.class), typeDescriptor(TestsAffectedByCurrentGitBranchIT.class)));
        store.commitTransaction();
    }

}

package org.jqassistant.contrib.plugin.testimpactanalysis.impact;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import org.hamcrest.MatcherAssert;
import org.jqassistant.contrib.plugin.testimpactanalysis.AbstractGitRuleIT;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.OtherTypeTest;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.TypeTest;

import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

abstract class AbstractTestImpactAnalysisRuleIT extends AbstractGitRuleIT {

    protected void verify(Class<?> changedType, String concept, Map<String, String> parameters) throws Exception {
        scanClassPathDirectory("a1", getClassesDirectory(AbstractTestImpactAnalysisRuleIT.class));

        Result<Concept> result = applyConcept(concept, parameters);

        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(1);
        Map<String, Object> row = rows.get(0);
        store.beginTransaction();
        ArtifactDescriptor artifact = (ArtifactDescriptor) row.get("Artifact");
        assertThat(artifact).isNotNull();
        assertThat(artifact.getFullQualifiedName()).isEqualTo("a1");
        List<TypeDescriptor> tests = (List<TypeDescriptor>) row.get("Tests");
        assertThat(tests).isNotNull();
        MatcherAssert.assertThat(tests, hasItems(typeDescriptor(TypeTest.class)));
        MatcherAssert.assertThat(tests, not(hasItems(typeDescriptor(OtherTypeTest.class))));
        MatcherAssert.assertThat(store.executeQuery("MATCH (t:Type:Changed) RETURN t").getSingleResult().get("t", TypeDescriptor.class), typeDescriptor(changedType));
        List<TypeDescriptor> affectedTests = store.executeQuery("MATCH (t:Type:Test:Affected) RETURN collect(t) as affectedTests").getSingleResult()
            .get("affectedTests", List.class);
        MatcherAssert.assertThat(affectedTests, hasItems(typeDescriptor(TypeTest.class), typeDescriptor(TestsAffectedByCurrentGitBranchIT.class)));
        store.commitTransaction();
    }

}

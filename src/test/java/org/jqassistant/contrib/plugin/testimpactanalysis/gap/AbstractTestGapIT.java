package org.jqassistant.contrib.plugin.testimpactanalysis.gap;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import org.jqassistant.contrib.plugin.testimpactanalysis.AbstractGitRuleIT;
import org.jqassistant.contrib.plugin.testimpactanalysis.gap.set.Type;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.TestsAffectedByCurrentGitBranchIT;

import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractTestGapIT extends AbstractGitRuleIT {

    public void verify(String constraint, Map<String, String> constraintParameters) throws Exception {
        scanClassPathDirectory("a1", getClassesDirectory(TestsAffectedByCurrentGitBranchIT.class));
        createGitHistory(Type.class);

        Result<Constraint> result = validateConstraint(constraint, constraintParameters);

        assertThat(result.getStatus()).isEqualTo(FAILURE);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(1);
        Map<String, Object> row = rows.get(0);
        store.beginTransaction();
        ArtifactDescriptor artifact = (ArtifactDescriptor) row.get("Artifact");
        assertThat(artifact).isNotNull();
        assertThat(artifact.getFullQualifiedName()).isEqualTo("a1");
        TypeDescriptor type = (TypeDescriptor) row.get("Type");
        assertThat(type).isNotNull();
        assertThat(type, typeDescriptor(Type.class));
        List<MethodDescriptor> methods = (List<MethodDescriptor>) row.get("Methods");
        assertThat(methods.size()).isEqualTo(1);
        assertThat(methods, hasItem(methodDescriptor(Type.class, "nonCoveredPublic")));
        store.commitTransaction();

    }

    protected abstract void createGitHistory(Class<?> changedType);
}

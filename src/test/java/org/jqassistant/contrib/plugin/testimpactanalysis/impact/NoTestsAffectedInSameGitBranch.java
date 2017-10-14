package org.jqassistant.contrib.plugin.testimpactanalysis.impact;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jqassistant.contrib.plugin.testimpactanalysis.AbstractGitRuleTest;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.OtherType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.Type;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;

public class NoTestsAffectedInSameGitBranch extends AbstractGitRuleTest {

    private static final String CONCEPT = "test-impact-analysis:TestsAffectedByCurrentGitBranch";

    @Test
    public void noTestsAffectedInSameGitBranch() throws Exception {
        scanClassPathDirectory("a1", getClassesDirectory(TestsAffectedByCurrentGitBranchTest.class));
        createGitHistory(Type.class);

        Result<Concept> result = applyConcept(CONCEPT, Collections.<String, String> emptyMap());

        assertThat(result.getStatus(), equalTo(FAILURE));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(0));
        store.beginTransaction();
        assertThat(store.executeQuery("MATCH (t:Type:Changed) RETURN count(t) as changedTypes").getSingleResult().get("changedTypes", Number.class).intValue(),
                equalTo(0));
        assertThat(store.executeQuery("MATCH (t:Type:Test:Affected) RETURN count(t) as affectedTests").getSingleResult().get("affectedTests", Number.class)
                .intValue(), equalTo(0));
        store.commitTransaction();
    }

    private void createGitHistory(Class<?> changedType) {
        store.beginTransaction();
        GitRepositoryDescriptor repository = store.create(GitRepositoryDescriptor.class);

        GitCommitDescriptor developHead = createCommit(OtherType.class);
        GitCommitDescriptor base = createCommit();
        developHead.getParents().add(base);
        GitBranchDescriptor developBranch = store.create(GitBranchDescriptor.class);
        developBranch.setName("heads/develop");
        developBranch.setHead(developHead);
        repository.getBranches().add(developBranch);

        GitCommitDescriptor masterHead = createCommit();
        GitCommitDescriptor parent = createCommit(changedType);
        masterHead.getParents().add(parent);
        parent.getParents().add(base);
        GitBranchDescriptor masterBranch = store.create(GitBranchDescriptor.class);
        masterBranch.setName("heads/master");
        masterBranch.setHead(masterHead);
        repository.setHead(masterHead);
        repository.getBranches().add(masterBranch);
        store.commitTransaction();
    }

}

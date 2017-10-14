package org.jqassistant.contrib.plugin.testimpactanalysis.impact;

import java.util.Collections;

import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.OtherType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.SubType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.SuperType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.Type;
import org.junit.Test;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;

public class TestsAffectedByLastGitCommitTest extends AbstractTestImpactAnalysisRuleTest {

    private static final String CONCEPT = "test-impact-analysis:TestsAffectedByLastGitCommit";

    @Test
    public void typeChanged() throws Exception {
        createGitHistory(Type.class);
        verify(Type.class, CONCEPT, Collections.<String, String> emptyMap());
    }

    @Test
    public void subTypeChanged() throws Exception {
        createGitHistory(SubType.class);
        verify(SubType.class, CONCEPT, Collections.<String, String> emptyMap());
    }

    @Test
    public void superTypeChanged() throws Exception {
        createGitHistory(SuperType.class);
        verify(SuperType.class, CONCEPT, Collections.<String, String> emptyMap());
    }

    private void createGitHistory(Class<?> changedType) {
        store.beginTransaction();
        GitCommitDescriptor head = createCommit(changedType);
        GitCommitDescriptor parent = createCommit(OtherType.class);
        head.getParents().add(parent);
        GitBranchDescriptor branch = store.create(GitBranchDescriptor.class);
        branch.setName("heads/master");
        branch.setHead(head);
        GitRepositoryDescriptor repository = store.create(GitRepositoryDescriptor.class);
        repository.setHead(head);
        repository.getBranches().add(branch);
        store.commitTransaction();
    }
}

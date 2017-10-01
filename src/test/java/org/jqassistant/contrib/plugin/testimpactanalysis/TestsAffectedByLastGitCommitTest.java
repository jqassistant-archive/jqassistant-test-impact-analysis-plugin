package org.jqassistant.contrib.plugin.testimpactanalysis;

import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.OtherType;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.SubType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.SuperType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.Type;
import org.junit.Ignore;
import org.junit.Test;

public class TestsAffectedByLastGitCommitTest extends AbstractTestImpactAnalysisRuleTest {

    @Test
    public void typeChanged() throws Exception {
        verify(Type.class);
    }

    @Test
    public void subTypeChanged() throws Exception {
        verify(SubType.class);
    }

    @Test
    @Ignore("Not yet implemented")
    public void superTypeChanged() throws Exception {
        verify(SuperType.class);
    }


    @Override
    protected String getConcept() {
        return "test-impact-analysis:TestsAffectedByLastGitCommit";
    }

    protected void createGitHistory(Class<?> changedType) {
        store.beginTransaction();
        GitCommitDescriptor parent = createCommit(OtherType.class);
        GitCommitDescriptor head = createCommit(changedType);
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

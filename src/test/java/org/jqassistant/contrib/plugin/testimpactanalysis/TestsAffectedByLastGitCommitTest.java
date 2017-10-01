package org.jqassistant.contrib.plugin.testimpactanalysis;

import java.util.Collections;
import java.util.Map;

import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.OtherType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.SubType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.SuperType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.Type;
import org.junit.Ignore;
import org.junit.Test;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;

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
    public void superTypeChanged() throws Exception {
        verify(SuperType.class);
    }

    @Override
    protected String getConcept() {
        return "test-impact-analysis:TestsAffectedByLastGitCommit";
    }

    @Override
    protected Map<String, String> getConceptParameters() {
        return Collections.emptyMap();
    }

    protected void createGitHistory(Class<?> changedType) {
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

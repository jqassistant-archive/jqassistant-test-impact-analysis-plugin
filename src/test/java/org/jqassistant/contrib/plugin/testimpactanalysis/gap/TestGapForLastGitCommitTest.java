package org.jqassistant.contrib.plugin.testimpactanalysis.gap;

import java.util.Collections;

import org.junit.Test;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;

public class TestGapForLastGitCommitTest extends AbstractTestGapTest {

    @Test
    public void gapForLastCommit() throws Exception {
        verify("test-impact-analysis:TestGapForLastGitCommit", Collections.<String, String>emptyMap());
    }

    @Override
    protected void createGitHistory(Class<?> changedType) {
        store.beginTransaction();
        GitCommitDescriptor head = createCommit(changedType);
        GitBranchDescriptor branch = store.create(GitBranchDescriptor.class);
        branch.setName("heads/master");
        branch.setHead(head);
        GitRepositoryDescriptor repository = store.create(GitRepositoryDescriptor.class);
        repository.setHead(head);
        repository.getBranches().add(branch);
        store.commitTransaction();
    }
}
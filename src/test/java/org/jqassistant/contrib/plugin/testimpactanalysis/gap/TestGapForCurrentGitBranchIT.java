package org.jqassistant.contrib.plugin.testimpactanalysis.gap;

import java.util.HashMap;

import org.junit.Test;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;

public class TestGapForCurrentGitBranchIT extends AbstractTestGapIT {

    @Test
    public void gapForLastCommit() throws Exception {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("testImpactAnalysisGitBaseBranch", "develop");
        verify("test-impact-analysis:TestGapForCurrentGitBranch", parameters);
    }

    @Override
    protected void createGitHistory(Class<?> changedType) {
        store.beginTransaction();
        GitRepositoryDescriptor repository = store.create(GitRepositoryDescriptor.class);
        GitCommitDescriptor developHead = createCommit();
        GitCommitDescriptor base = createCommit();
        developHead.getParents().add(base);
        GitBranchDescriptor developBranch = store.create(GitBranchDescriptor.class);
        developBranch.setName("heads/develop");
        developBranch.setHead(developHead);
        repository.getBranches().add(developBranch);

        GitCommitDescriptor featureHead = createCommit();
        GitCommitDescriptor featureParent = createCommit(changedType);
        featureHead.getParents().add(featureParent);
        featureParent.getParents().add(base);
        GitBranchDescriptor featureBranch = store.create(GitBranchDescriptor.class);
        featureBranch.setName("heads/feature/test-feature");
        featureBranch.setHead(featureHead);
        repository.getBranches().add(featureBranch);

        repository.setHead(featureHead);

        store.commitTransaction();
    }
}

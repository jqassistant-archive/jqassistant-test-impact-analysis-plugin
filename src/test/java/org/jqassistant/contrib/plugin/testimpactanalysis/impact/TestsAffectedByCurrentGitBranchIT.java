package org.jqassistant.contrib.plugin.testimpactanalysis.impact;

import java.util.HashMap;
import java.util.Map;

import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.OtherType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.SubType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.SuperType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.Type;
import org.junit.Test;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;

public class TestsAffectedByCurrentGitBranchIT extends AbstractTestImpactAnalysisRuleIT {

    private static final String CONCEPT = "test-impact-analysis:TestsAffectedByCurrentGitBranch";

    @Test
    public void typeChanged() throws Exception {
        createGitHistory(Type.class);
        verify(Type.class, CONCEPT, getRuleParameters());
    }

    @Test
    public void subTypeChanged() throws Exception {
        createGitHistory(SubType.class);
        verify(SubType.class, CONCEPT, getRuleParameters());
    }

    @Test
    public void superTypeChanged() throws Exception {
        createGitHistory(SuperType.class);
        verify(SuperType.class, CONCEPT, getRuleParameters());
    }

    private Map<String, String> getRuleParameters() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("testImpactAnalysisGitBaseBranch", "develop");
        return parameters;
    }

    private void createGitHistory(Class<?> changedType) {
        store.beginTransaction();
        GitRepositoryDescriptor repository = store.create(GitRepositoryDescriptor.class);
        GitCommitDescriptor developHead = createCommit(OtherType.class);
        GitCommitDescriptor base = createCommit(OtherType.class);
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

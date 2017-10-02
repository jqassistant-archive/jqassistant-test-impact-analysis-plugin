package org.jqassistant.contrib.plugin.testimpactanalysis;

import java.util.HashMap;
import java.util.Map;

import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.OtherType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.SubType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.SuperType;
import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.Type;
import org.junit.Test;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;

public class TestsAffectedByCurrentGitBranchTest extends AbstractTestImpactAnalysisRuleTest {

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
        return "test-impact-analysis:TestsAffectedByCurrentGitBranch";
    }

    @Override
    protected Map<String, String> getConceptParameters() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("testImpactAnalysisGitBaseBranch", "test-feature");
        return parameters;
    }

    protected void createGitHistory(Class<?> changedType) {
        store.beginTransaction();
        GitRepositoryDescriptor repository = store.create(GitRepositoryDescriptor.class);
        GitCommitDescriptor developHead = createCommit(OtherType.class);
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

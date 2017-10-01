package org.jqassistant.contrib.plugin.testimpactanalysis;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jqassistant.contrib.plugin.testimpactanalysis.set.rules.*;
import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.*;

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
    @Ignore("Not yet implemented")
    public void superTypeChanged() throws Exception {
        verify(SuperType.class);
    }

    @Override
    protected String getConcept() {
        return "test-impact-analysis:TestsAffectedByCurrentGitBranch";
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

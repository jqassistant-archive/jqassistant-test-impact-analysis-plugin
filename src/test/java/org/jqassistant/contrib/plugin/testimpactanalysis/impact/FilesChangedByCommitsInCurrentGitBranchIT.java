package org.jqassistant.contrib.plugin.testimpactanalysis.impact;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitRepositoryDescriptor;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.OtherType;
import org.jqassistant.contrib.plugin.testimpactanalysis.impact.set.Type;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

public class FilesChangedByCommitsInCurrentGitBranchIT extends AbstractTestImpactAnalysisRuleIT {

    private static final String CONCEPT = "test-impact-analysis:FilesChangedByCommitsInCurrentGitBranch";

    @Test
    public void baseBranchNoCommitAhead() throws Exception {
        verify(0);
    }

    @Test
    public void baseBranchOneCommitAhead() {
        createGitHistory(Type.class, 1);
    }

    private void verify(int developOffset) throws Exception {
        createGitHistory(Type.class, developOffset);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("testImpactAnalysisGitBaseBranch", "develop");

        Result<Concept> result = applyConcept(CONCEPT, parameters);

        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(1);
        Map<String, Object> row = rows.get(0);
        GitCommitDescriptor commit = (GitCommitDescriptor) row.get("Commit");
        assertThat(commit).isNotNull();
        List<String> filesChangedByCommit = (List<String>) row.get("FilesChangedByCommit");
        assertThat(filesChangedByCommit.size()).isEqualTo(1);
        String file = filesChangedByCommit.get(0);
        assertThat(file).endsWith(getSourceFileName(Type.class));
    }

    private void createGitHistory(Class<?> changedType, int developOffset) {
        store.beginTransaction();
        GitRepositoryDescriptor repository = store.create(GitRepositoryDescriptor.class);
        // create base commit with a parent
        GitCommitDescriptor base = createCommit(OtherType.class);
        GitCommitDescriptor baseParent = createCommit(OtherType.class);
        base.getParents().add(baseParent);
        // create develop branch with given offset
        GitCommitDescriptor developHead = base;
        for (int i = 0; i < developOffset; i++) {
            GitCommitDescriptor head = createCommit(OtherType.class);
            head.getParents().add(developHead);
            developHead = head;
        }
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

package org.jqassistant.contrib.plugin.testimpactanalysis.impact;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitBranchDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import org.assertj.core.api.Assertions;
import org.jqassistant.contrib.plugin.testimpactanalysis.AbstractGitRuleIT;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class GitBaseCommitIT extends AbstractGitRuleIT {

    private static final String CONCEPT = "test-impact-analysis:GitBaseCommit";

    @Test
    public void baseCommits() throws Exception {
        store.beginTransaction();
        GitCommitDescriptor root = store.create(GitCommitDescriptor.class);
        GitCommitDescriptor base1 = store.create(GitCommitDescriptor.class);
        base1.getParents().add(root);
        GitCommitDescriptor feature1Head = store.create(GitCommitDescriptor.class);
        feature1Head.getParents().add(base1);
        GitCommitDescriptor base2 = store.create(GitCommitDescriptor.class);
        base2.getParents().add(base1);
        GitCommitDescriptor feature2Head = store.create(GitCommitDescriptor.class);
        feature2Head.getParents().add(base2);
        GitBranchDescriptor featureBranch1 = store.create(GitBranchDescriptor.class);
        GitBranchDescriptor featureBranch2 = store.create(GitBranchDescriptor.class);
        GitBranchDescriptor developBranch = store.create(GitBranchDescriptor.class);
        featureBranch1.setHead(base1);
        developBranch.setHead(base2);
        featureBranch2.setHead(feature2Head);
        store.commitTransaction();

        Result<Concept> result = applyConcept(CONCEPT);

        Assertions.assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        Assertions.assertThat(rows.size()).isEqualTo(2);

        List<GitCommitDescriptor> baseCommits = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            baseCommits.add((GitCommitDescriptor) row.get("Base"));
        }
        assertThat(baseCommits, hasItems(base1, base2));
    }

}

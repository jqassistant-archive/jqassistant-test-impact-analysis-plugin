package org.jqassistant.contrib.plugin.testimpactanalysis;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitChangeDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitCommitDescriptor;
import de.kontext_e.jqassistant.plugin.git.store.descriptor.GitFileDescriptor;

public abstract class AbstractGitRuleTest extends AbstractJavaPluginIT {

    protected String getSourceFileName(Class<?> type) {
        String sourceDirectory = "/src/test/java/";
        return sourceDirectory + type.getName().replace('.', '/') + ".java";
    }

    protected GitCommitDescriptor createCommit(Class<?>... types) {
        GitCommitDescriptor commit = store.create(GitCommitDescriptor.class);
        for (Class<?> type : types) {
            GitChangeDescriptor change = store.create(GitChangeDescriptor.class);
            GitFileDescriptor file = store.create(GitFileDescriptor.class);
            file.setRelativePath(getSourceFileName(type));
            change.setModifies(file);
            commit.getFiles().add(change);
        }
        return commit;
    }

}

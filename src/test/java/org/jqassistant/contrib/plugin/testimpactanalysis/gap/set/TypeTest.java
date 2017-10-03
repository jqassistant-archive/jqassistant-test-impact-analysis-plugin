package org.jqassistant.contrib.plugin.testimpactanalysis.gap.set;

import org.junit.Test;

public class TypeTest {

    private Type type = new Type();

    @Test
    public void coveredMethods() {
        type.coveredDefault();
        type.coveredProtected();
        type.coveredPublic();
    }

}

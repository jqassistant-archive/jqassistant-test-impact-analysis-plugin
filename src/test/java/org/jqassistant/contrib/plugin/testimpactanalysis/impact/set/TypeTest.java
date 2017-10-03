package org.jqassistant.contrib.plugin.testimpactanalysis.impact.set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TypeTest {

    @Test
    public void typeTest() {
        assertThat(new Type(), notNullValue());
    }

}
